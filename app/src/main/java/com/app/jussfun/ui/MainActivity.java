package com.app.jussfun.ui;

import static com.app.jussfun.utils.Constants.POP_UP_WINDOW_PERMISSION_ASK_AGAIN;
import static com.app.jussfun.utils.Constants.isCallPopup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.external.CustomViewPager;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.callback.FingerPrintCallBack;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.OnOkClickListener;
import com.app.jussfun.model.AddDeviceRequest;
import com.app.jussfun.model.AdminMessageResponse;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.ChatResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.MembershipPackages;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.model.ReferFriendResponse;
import com.app.jussfun.model.RenewalRequest;
import com.app.jussfun.model.SignInRequest;
import com.app.jussfun.model.SignInResponse;
import com.app.jussfun.ui.feed.CommentsActivity;
import com.app.jussfun.ui.feed.FeedsActivity;
import com.app.jussfun.ui.feed.FeedsFragment;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.DeviceTokenPref;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseFragmentActivity implements PurchasesUpdatedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.viewPager)
    CustomViewPager viewPager;
    @BindView(R.id.bottom_navigation)
    public
    BottomNavigationView bottomNavigation;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.splashLay)
    RelativeLayout splashLay;
    Boolean isSettingsIn = false;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor1;

    HomeFragment randomFragment;
    FeedsFragment feedsFragment;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;


    public static ProfileResponse profileResponse;
    private MenuItem prevMenuItem;
    private ApiInterface apiInterface;
    DialogProfile profileDialog;
    DialogProfileImage imageDialog;
    DialogFreeGems gemsDialog;
    boolean doubleBackToExit = false;
    private BillingClient billingClient;
    Handler onlineHandler = new Handler(Looper.getMainLooper());
    int delay = 5000; //milliseconds
    DBHelper dbHelper;
    List<String> skuList = new ArrayList<>();
    private int NUM_PAGES = 4;
    private String from = null;
    private BiometricPrompt.PromptInfo promptInfo;
    private ExecutorService executor;
    private BiometricPrompt biometricPrompt;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    Cipher defaultCipher;
    Cipher cipherNotInvalidated;
    private AppUtils appUtils;
    Boolean isAutoStartIn = false;

    DialogOverLayPermission dialogOverLayPermission;

    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkReceiver.isConnected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_TIMESTAMP, AppUtils.getCurrentUTCTime(getApplicationContext()));
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_UPDATE_LIVE);
                    jsonObject.put(Constants.TAG_LIVE_STATUS, AppUtils.getCurrentStatus());
                    Log.i(TAG, "_updateLive: " + jsonObject.toString());
                    AppWebSocket.getInstance(MainActivity.this).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            onlineHandler.postDelayed(onlineRunnable, delay);
        }
    };
    public int bottomNavHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        executor = Executors.newSingleThreadExecutor();
        checkUserIsActive();
        setupViewPager(viewPager);
        initPermissionDialog();

        preferences = getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = preferences.edit();

        sharedPrefs = getSharedPreferences("MyPref", 0);
        editor1 = sharedPrefs.edit();
        dbHelper = DBHelper.getInstance(this);
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        Log.d(TAG, "onCreate: " + GetSet.getAuthToken());
        registerNetworkReceiver();

        if (getIntent().hasExtra(Constants.NOTIFICATION)) {
            from = getIntent().getStringExtra(Constants.NOTIFICATION);
            if (getIntent().getBooleanExtra(Constants.TAG_FROM_FOREGROUND, false)) {
                checkFromNotification();
            } else {
                if (SharedPref.getBoolean(SharedPref.IS_APP_OPENED, false)) {
                    checkFromNotification();
                } else {
                    if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
                        splashLay.setVisibility(View.VISIBLE);
                        bottomNavigation.setVisibility(View.INVISIBLE);
                        checkFingerPrintEnabled();
                    } else {
                        SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
                        checkFromNotification();
                    }
                }
            }
        } else if (from != null && from.equals(Constants.TAG_COMMENT_FEEDS) && getIntent().hasExtra(Constants.TAG_FEED_ID)) {
            Intent feedIntent = new Intent(getApplicationContext(), CommentsActivity.class);
            feedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            feedIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
            feedIntent.putExtra(Constants.TAG_FEED_ID, getIntent().getStringExtra(Constants.TAG_FEED_ID));
            feedIntent.putExtra(Constants.TAG_FROM, getIntent().getStringExtra(Constants.NOTIFICATION));
            startActivity(feedIntent);

        } else if (getIntent().hasExtra(Constants.TAG_FEED_ID)) {
            Intent feedIntent = new Intent(getApplicationContext(), FeedsActivity.class);
            feedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            feedIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
            feedIntent.putExtra(Constants.TAG_FEED_ID, getIntent().getStringExtra(Constants.TAG_FEED_ID));
            startActivity(feedIntent);

        }

        getGooglePlayCurrency();

//        showProfileDialog();
//        showGemsDialog();
//        showProfileImageDialog();

        if (GetSet.getName() == null) {
            showProfileDialog();
        } else {
            showGemsDialog();
            getUserProfile(GetSet.getUserId());
        }
        sendPing();
        //     getBottomNavHeight();

    }

    private void checkFromNotification() {
        SharedPref.putBoolean(SharedPref.IS_APP_OPENED, true);
        splashLay.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
        switch (from) {
            case Constants.TAG_FOLLOW: {
                Intent profileIntent = new Intent(getApplicationContext(), OthersProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PARTNER_ID, getIntent().getStringExtra(Constants.TAG_PARTNER_ID));
                profileIntent.putExtra(Constants.TAG_FROM, Constants.NOTIFICATION);
//                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_PROFILE: {
                Intent profileIntent = new Intent(getApplicationContext(), ChatActivity.class);
                profileIntent.putExtra(Constants.TAG_PARTNER_ID, getIntent().getStringExtra(Constants.TAG_PARTNER_ID));
                profileIntent.putExtra(Constants.TAG_PARTNER_NAME, getIntent().getStringExtra(Constants.TAG_PARTNER_NAME));
                profileIntent.putExtra(Constants.TAG_PARTNER_IMAGE, getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE));
//                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_ADMIN: {
                Intent profileIntent = new Intent(getApplicationContext(), ChatActivity.class);
                ChatResponse adminChat = dbHelper.getAdminChat(GetSet.getUserId());
                profileIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
                profileIntent.putExtra(Constants.TAG_PARTNER_NAME, adminChat.getUserName());
//                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_INTEREST: {
                Intent profileIntent = new Intent(getApplicationContext(), FollowersActivity.class);
                profileIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                profileIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
//                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_MATCH: {
                Intent profileIntent = new Intent(getApplicationContext(), FollowersActivity.class);
                profileIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                profileIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
//                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_FEEDS: {
                Intent feedIntent = new Intent(getApplicationContext(), FeedsActivity.class);
                feedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                feedIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                feedIntent.putExtra(Constants.TAG_FEED_ID, getIntent().getStringExtra(Constants.TAG_FEED_ID));
                startActivity(feedIntent);
            }
            default:
                break;
        }
    }

    private void checkUserIsActive() {
        Call<HashMap<String, String>> call = apiInterface.isActive(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();
                if (Response.get(Constants.TAG_STATUS).equals(Constants.TAG_FALSE)) {
                    App.makeToast(getString(R.string.account_deactivated_by_admin));
                    GetSet.reset();
                    SharedPref.clearAll();
                    DBHelper.getInstance(MainActivity.this).clearDB();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    private void setupViewPager(CustomViewPager viewPager) {
        bottomNavigation.setItemIconTintList(null);
        viewPager.setOffscreenPageLimit(NUM_PAGES);
        viewPager.setPagingEnabled(false);

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
        setBottomNavigation(0);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                    case 1:
                    case 2:
                        resetTyping();
                        break;
                    default:
                        break;
                }

                if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);
                else {
                    bottomNavigation.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigation.getMenu().getItem(position);
                setBottomNavigation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuFeeds:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.menuHome:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.menuChat:
                        viewPager.setCurrentItem(2);
                        break;
                    case R.id.menuProfile:
                        viewPager.setCurrentItem(3);
                        break;
                }
                return false;
            }
        });
    }

    private void resetTyping() {
        if (chatFragment != null) {
            dbHelper.updateChatsTyping();
            dbHelper.updateChatsOnline();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
        getAppDefaultData();
        if (isConnected && GetSet.isIsLogged() && GetSet.getUserId() != null) {
            onlineHandler.post(onlineRunnable);
        } else {
            onlineHandler.removeCallbacks(onlineRunnable);
        }

    }

    private void sendPing() {
        if (GetSet.isLogged() && GetSet.getUserId() != null) {
            onlineHandler.post(onlineRunnable);
        }
    }

    private void removePing() {
        onlineHandler.removeCallbacks(onlineRunnable);
    }

    public class MainPagerAdapter extends FragmentPagerAdapter {

        Activity activity;

        public MainPagerAdapter(FragmentManager fm, MainActivity mainActivity) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            activity = mainActivity;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                default:
                    randomFragment = new HomeFragment();
                    return randomFragment;

                case 1:
                    feedsFragment = new FeedsFragment();
                    return feedsFragment;

                case 2:
                    chatFragment = new ChatFragment();
                    Bundle chat = new Bundle();
                    chat.putInt(Constants.TAG_BOTTOM_HEIGHT, bottomNavHeight);
                    chatFragment.setArguments(chat);
                    return chatFragment;

                case 3:
                    profileFragment = new ProfileFragment();
                    return profileFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private void setBottomNavigation(int position) {
        switch (position) {
            case 0:
                bottomNavigation.getMenu().findItem(R.id.menuFeeds).setIcon(getDrawable(R.drawable.ic_feed_inactive));
                bottomNavigation.getMenu().findItem(R.id.menuHome).setIcon(getDrawable(R.drawable.main_discover_p));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.main_chat_plan));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.main_me_p));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBottomNavigation));
                break;
            case 1:
                bottomNavigation.getMenu().findItem(R.id.menuFeeds).setIcon(getDrawable(R.drawable.ic_feed_active));
                bottomNavigation.getMenu().findItem(R.id.menuHome).setIcon(getDrawable(R.drawable.main_discover));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.main_chat_plan));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.main_me_p));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBottomNavigation));
                break;
            case 2:
                bottomNavigation.getMenu().findItem(R.id.menuFeeds).setIcon(getDrawable(R.drawable.ic_feed_inactive));
                bottomNavigation.getMenu().findItem(R.id.menuHome).setIcon(getDrawable(R.drawable.main_discover));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.main_chat_p));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.main_me_p));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBottomNavigation));
                break;
            case 3:
                bottomNavigation.getMenu().findItem(R.id.menuFeeds).setIcon(getDrawable(R.drawable.ic_feed_inactive));
                bottomNavigation.getMenu().findItem(R.id.menuHome).setIcon(getDrawable(R.drawable.main_discover));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.main_chat_plan));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.main_me_plan));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBottomNavigation));
                break;
        }
    }

    public void checkFingerPrintEnabled() {
        if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to get an instance of KeyStore", e);
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mKeyGenerator = KeyGenerator
                            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                }
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
            }

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    openBioMetricDialog();
//                    openFingerPrintDialog();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openFingerPrintDialog();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

                if (fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints()) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                    openFingerPrintDialog();
                } else if (km != null && km.isKeyguardSecure()) {
                    Intent i = km.createConfirmDeviceCredentialIntent(getString(R.string.authentication_required), getString(R.string.password));
                    startActivityForResult(i, Constants.DEVICE_LOCK_REQUEST_CODE);
                } else {
                    checkFromNotification();
                }
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
            checkFromNotification();
        }
    }

    private void openBioMetricDialog() {
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.i(TAG, "onAuthenticationError: " + errorCode);
                if (errorCode == 7) {
                    App.makeToast(getString(R.string.too_many_attempts));
                } else if (errorCode == 13) {
                    Logging.i(TAG, "onAuthenticationError: " + "Cancel");
                }
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkFromNotification();
                    }
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.security))
                .setSubtitle("")
                .setDescription(getString(R.string.touch_fingerprint_description))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        biometricPrompt.authenticate(promptInfo);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openFingerPrintDialog() {
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
        if (fingerprintManager != null && !fingerprintManager.hasEnrolledFingerprints()) {
            checkFromNotification();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initCipher(defaultCipher, Constants.DEFAULT_KEY_NAME);
            }
            DialogFingerPrint dialogFingerPrint = new DialogFingerPrint();
            dialogFingerPrint.setContext(MainActivity.this);
            dialogFingerPrint.setCallBack(new FingerPrintCallBack() {
                @Override
                public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {
                    if (withFingerprint) {
                        // If the user has authenticated with fingerprint, verify that using cryptography and
                        // then show the confirmation message.
                        assert cryptoObject != null;
                        tryEncrypt(cryptoObject.getCipher());
                    } else {
                        // Authentication happened with backup password. Just show the confirmation message.

                    }
                }

                @Override
                public void onError(String errorMsg) {
                    App.makeToast(errorMsg);
                }
            });
            dialogFingerPrint.setCryptoObject(new FingerprintManager.CryptoObject(defaultCipher));
            dialogFingerPrint.show(getSupportFragmentManager(), TAG);
        }
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName                          the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(Constants.SECRET_MESSAGE.getBytes());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkFromNotification();
                }
            });
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            App.makeToast("Failed to encrypt the data with the generated key. " + "Retry the purchase");
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEVICE_LOCK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkFromNotification();
            } else {
                App.makeToast(getString(R.string.unable_to_verify));
                finish();
            }
        }

        if (requestCode == Constants.OVERLAY_REQUEST_CODE_TECNO) {
            if (!checkOverLayPermission()) {
                openPermissionDialog();
//                    miOverLayPermission();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.isInRandomCall = false;
        Constants.isInVideoCall = false;
        RandomCallActivity.isInCall = false;
        VideoCallActivity.isInCall = false;
        if (randomFragment != null) {
            randomFragment.updateGemsCount();
            randomFragment.updateFilterView();
        }
        AppUtils.setCurrentStatus(Constants.ONLINE);


        if (checkOverLayPermission()) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableProtectesApps();
            }
        }

        if (isSettingsIn) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableAutoStart();
            }

        }


        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }


        if (!isCallPopup) {
            if (preferences.getString(POP_UP_WINDOW_PERMISSION_ASK_AGAIN, "false").equalsIgnoreCase("false")) {
                if (!checkOverLayPermission())
                    openPermissionDialog();
            }
        }
    }

    @Override
    protected void onDestroy() {
        AppUtils.resetFilter();
        removePing();
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        billingClient.endConnection();
    }

    private void showProfileDialog() {
        profileDialog = new DialogProfile();
        profileDialog.setContext(MainActivity.this);
        profileDialog.setCallBack(new OnOkClickListener() {
            @Override
            public void onOkClicked(Object object) {
                if (NetworkReceiver.isConnected()) {
                    SignInRequest request = (SignInRequest) object;
                    signUp(request);
                } else {
                    AppUtils.showSnack(MainActivity.this, findViewById(R.id.parentLay), false);
                }
            }
        });
        profileDialog.setCancelable(false);
        profileDialog.show(getSupportFragmentManager(), TAG);
    }

    private void signUp(SignInRequest request) {
        profileDialog.showLoading();
        Call<SignInResponse> call = apiInterface.callSignUp(request);
        call.enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                SignInResponse signin = response.body();
                if (signin.getStatus().equals(Constants.TAG_TRUE)) {
                    GetSet.setUserId(signin.getUserId());
                    GetSet.setLoginId(signin.getLoginId());
                    GetSet.setName(signin.getName());
                    GetSet.setDob(signin.getDob());
                    GetSet.setAge(signin.getAge());
                    GetSet.setGender(signin.getGender());
                    GetSet.setAuthToken(signin.getAuthToken());
                    GetSet.setFollowersCount("0");
                    GetSet.setFollowingCount("0");
                    GetSet.setFriendsCount(0);
                    GetSet.setInterestsCount(0);
                    GetSet.setUnlocksLeft(0);
                    GetSet.setGems(0L);
                    GetSet.setGifts(0L);
                    GetSet.setPremiumMember(Constants.TAG_FALSE);
                    GetSet.setPremiumExpiry("");
                    GetSet.setPrivacyAge(signin.getPrivacyAge());
                    GetSet.setPrivacyContactMe(signin.getPrivacyContactMe());
                    GetSet.setFollowNotification(signin.getFollowNotification());
                    GetSet.setChatNotification(signin.getChatNotification());
                    GetSet.setShowNotification(signin.getShowNotification());
                    GetSet.setInterestNotification(signin.getInterestNotification());

                    SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                    SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                    SharedPref.putString(SharedPref.NAME, GetSet.getName());
                    SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                    SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                    SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                    SharedPref.putString(SharedPref.AUTH_TOKEN, GetSet.getAuthToken());
                    SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                    SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                    SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                    SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                    SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
                    SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                    SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                    SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                    SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                    SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                    SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                    SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                    SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                    SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                    SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                    SharedPref.putBoolean(SharedPref.INTEREST_NOTIFICATION, GetSet.getInterestNotification());
                    apiInterface = ApiClient.getClient().create(ApiInterface.class);

                    if (!TextUtils.isEmpty(SharedPref.getString(SharedPref.REFERAL_CODE, ""))) {
                        updateGems(SharedPref.getString(SharedPref.REFERAL_CODE, ""));
                    }
                    AppUtils.callerList.clear();
                    AppUtils.callerList.add(GetSet.getUserId());
                    addDeviceID();

                } else {
                    profileDialog.hideLoading();
                    if (signin.getMessage().equals("Something went to be wrong")) {
                        App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(signin.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                call.cancel();
                profileDialog.hideLoading();
            }
        });
    }

    private void addDeviceID() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());

        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

                if (GetSet.getUserId() != null) {
                    AppWebSocket.mInstance = null;
                    AppWebSocket.getInstance(MainActivity.this);
                }
                AdminMessageResponse.MessageData messageData = new AdminMessageResponse().new MessageData();
                messageData.setMsgFrom(Constants.TAG_ADMIN);
                messageData.setMsgId("0");
                messageData.setMsgType(Constants.TAG_TEXT);
                messageData.setMsgData(AdminData.welcomeMessage);
                String msgAt = "" + (System.currentTimeMillis() / 1000);
                String createdAt = AppUtils.getCurrentUTCTime(getApplicationContext());
                messageData.setMsgAt(msgAt);
                messageData.setCreateaAt(createdAt);
                dbHelper.addAdminMessage(messageData);
                messageData.setMsgAt(createdAt);
                messageData.setCreateaAt(msgAt);
                int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
                dbHelper.addAdminRecentMessage(messageData, unseenCount);
                getUserProfile(GetSet.getUserId());

                if (SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null) != null) {
                    try {
                        InputStream imageStream = new URL(SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null)).openStream();
                        uploadImage(AppUtils.getBytes(imageStream));
                    } catch (Exception e) {
                        Logging.e(TAG, "onResponse: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    profileDialog.hideLoading();
                    profileDialog.dismiss();
                    showProfileImageDialog();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void updateGems(String referalCode) {
        Log.e(TAG, "updateGems: " + referalCode);
        Call<ReferFriendResponse> call = apiInterface.updateReferal(referalCode);
        call.enqueue(new Callback<ReferFriendResponse>() {
            @Override
            public void onResponse(Call<ReferFriendResponse> call, Response<ReferFriendResponse> response) {
                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    SharedPref.putString(SharedPref.REFERAL_CODE, "");
                }
            }

            @Override
            public void onFailure(Call<ReferFriendResponse> call, Throwable t) {

            }
        });
    }

    private void showProfileImageDialog() {
        imageDialog = new DialogProfileImage();
        imageDialog.setContext(MainActivity.this);
        imageDialog.setCallBack(new OnOkClickListener() {
            @Override
            public void onOkClicked(Object object) {
                InputStream imageStream = (InputStream) object;
                if (imageStream == null) {
                    imageDialog.dismiss();
                    if (AdminData.freeGems > 0) {
                        showGemsDialog();
                    }
                } else {
                    try {
                        uploadImage(AppUtils.getBytes(imageStream));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        imageDialog.setCancelable(false);
        imageDialog.show(getSupportFragmentManager(), TAG);
    }

    private void uploadImage(byte[] imageBytes) {
        if (imageDialog != null) {
            imageDialog.showLoading();
        }
        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    GetSet.setUserImage(data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, data.get(Constants.TAG_USER_IMAGE));
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                        imageDialog.dismiss();
                    }
                    if (profileDialog != null) {
                        profileDialog.hideLoading();
                        profileDialog.dismiss();
                    }
                    getUserProfile(GetSet.getUserId());
                    if (AdminData.freeGems > 0) {
                        showGemsDialog();
                    }
                } else if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_REJECTED)) {
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }
                    if (profileDialog != null) {
                        profileDialog.dismiss();
                    }
                    App.makeToast(data.get(Constants.TAG_MESSAGE));
                } else {
                    App.makeToast(data.get(Constants.TAG_MESSAGE));
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (imageDialog != null) {
                    imageDialog.hideLoading();
                }
                call.cancel();
            }
        });
    }

    private void showGemsDialog() {
        gemsDialog = new DialogFreeGems();
        gemsDialog.setContext(MainActivity.this);
        gemsDialog.setCallBack(new OnOkClickListener() {
            @Override
            public void onOkClicked(Object o) {
                gemsDialog.dismiss();
            }
        });
        gemsDialog.setCancelable(false);
        gemsDialog.show(getSupportFragmentManager(), TAG);
    }

    private void getUserProfile(String userId) {
        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(userId);
                request.setProfileId(userId);
                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            profileResponse = profile;
                            GetSet.setUserId(profile.getUserId());
                            GetSet.setLoginId(profile.getLoginId());
                            GetSet.setName(profile.getName());
                            GetSet.setDob(profile.getDob());
                            GetSet.setAge(profile.getAge());
                            GetSet.setGender(profile.getGender());
                            GetSet.setUserImage(profile.getUserImage());
                            GetSet.setLocation(profile.getLocation());
                            GetSet.setFollowingCount(profile.getFollowings());
                            GetSet.setFollowersCount(profile.getFollowers());
                            GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(profile.getAvailableGifts()) : 0L);
                            GetSet.setGems(profile.getAvailableGems() != null ? Long.parseLong(profile.getAvailableGems()) : 0L);
                            GetSet.setPremiumMember(profile.getPremiumMember());
                            GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                            GetSet.setPrivacyAge(profile.getPrivacyAge());
                            GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());
                            GetSet.setShowNotification(profile.getShowNotification());
                            GetSet.setFollowNotification(profile.getFollowNotification());
                            GetSet.setChatNotification(profile.getChatNotification());
                            GetSet.setGiftEarnings(profile.getGiftEarnings());
                            Log.d(TAG, "onResponseGiftEarning: " + profile.getGiftEarnings());
                            GetSet.setReferalLink(profile.getReferalLink());
                            GetSet.setCreatedAt(profile.getCreatedAt());
                            GetSet.setInterestsCount(profile.getInterests());
                            GetSet.setFriendsCount(profile.getFriends());

                            GetSet.setGiftCoversionEarnings(profile.getGiftCoversionEarnings());
                            GetSet.setGiftConversionValue(profile.getGiftConversionValue());
                            GetSet.setPaypal_id(profile.getPaypalId());

                            SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                            SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                            SharedPref.putString(SharedPref.NAME, GetSet.getName());
                            SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                            SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                            SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                            SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                            SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());
                            SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                            SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                            SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                            SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                            SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                            SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                            SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                            SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                            SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                            SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                            SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                            SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                            SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                            SharedPref.putString(SharedPref.REFERAL_LINK, GetSet.getReferalLink());
                            SharedPref.putString(SharedPref.CREATED_AT, GetSet.getCreatedAt());
                            SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                            SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());

                            SharedPref.putString(SharedPref.GIFT_CONVERSION_EARNINGS, GetSet.getGiftCoversionEarnings());
                            SharedPref.putString(SharedPref.GIFT_CONVERSION_VALUE, GetSet.getGiftConversionValue());
                            SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

                            if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                                AppUtils.filterLocation = new ArrayList<>();
                            }
                            if (randomFragment != null && randomFragment.isVisible()) {
                                randomFragment.updateProfile();
                            }

                            if (chatFragment != null && chatFragment.isVisible()) {
                                chatFragment.getMessageList(0);
                            }

                            if (profileFragment != null && profileFragment.isVisible()) {
                                profileFragment.updateProfile();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {

                    }
                });
            }
        } else {
//            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed();
            overridePendingTransition(0, 0);
            return;
        }

        if (viewPager != null && viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        } else {
            this.doubleBackToExit = true;
            App.makeToast(getString(R.string.back_button_exit_description));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExit = false;
                }
            }, 2000);
        }
    }

    private void getGooglePlayCurrency() {
        if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
            for (MembershipPackages membershipPackages : AdminData.membershipList) {
                skuList.add(membershipPackages.getSubsTitle());
            }
        } else {
            skuList.add(SharedPref.getString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU));
        }
        Log.i(TAG, "getGooglePlayCurrency: " + skuList);
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.


                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult1, List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                    for (SkuDetails skuDetails : skuDetailsList) {
                                        String sku = skuDetails.getSku();
                                        String price = skuDetails.getPrice();
                                        Log.i(TAG, "onSkuDetailsResponse: " + skuDetails);
                                        String validity = skuDetails.getSubscriptionPeriod();
                                        if (SharedPref.getString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU).equals(sku)) {
                                            SharedPref.putString(SharedPref.IN_APP_PRICE, price);
                                            SharedPref.putString(SharedPref.IN_APP_VALIDITY, validity);
                                            break;
                                        }
                                    }
                                    //  if(getIntent().getStringExtra("OnCLick") != null && getIntent().getStringExtra("OnCLick").equals("ClickHere")) {
                                    if (GetSet.getUserId() != null) {
                                        querySubscriptions(billingResult1);
                                    }
                                    //   }
                                }
                            });
                }

            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (purchases != null && purchases.size() > 0) {
            Log.i(TAG, "onPurchasesUpdated: " + purchases.size());
            Log.i(TAG, "onPurchasesUpdated: " + purchases.get(purchases.size() - 1).getSkus());
            /* Purchases size should be 1. */
            billingClient.endConnection();
            RenewalRequest request = new RenewalRequest();
            request.setUserId(GetSet.getUserId());
            request.setPackageId(purchases.get(purchases.size() - 1).getSkus().get(0));
            request.setRenewalTime(GetSet.getPremiumExpiry());
            Call<HashMap<String, String>> call = apiInterface.verifyPayment(request);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    HashMap<String, String> hashMap = response.body();
                    if (hashMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        getUserProfile(GetSet.getUserId());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

                }
            });
        }
    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    AppDefaultResponse defaultData = response.body();
                    if (defaultData.getStatus().equals(Constants.TAG_TRUE)) {
                        AdminData.resetData();
                        AdminData.freeGems = defaultData.getFreeGems();
                        AdminData.giftList = defaultData.getGifts();
                        AdminData.giftsDetails = defaultData.getGiftsDetails();
                        AdminData.primeDetails = defaultData.getPrimeDetails();
                        AdminData.reportList = defaultData.getReports();
                        /*Add first item as Select all location filter*/
                        AdminData.locationList = new ArrayList<>();
                        AdminData.locationList.add(getString(R.string.select_all));
                        AdminData.locationList.addAll(defaultData.getLocations());
                        AdminData.membershipList = defaultData.getMembershipPackages();
                        AdminData.filterGems = defaultData.getFilterGems();
                        AdminData.filterOptions = defaultData.getFilterOptions();
                        AdminData.inviteCredits = defaultData.getInviteCredits();
                        AdminData.showAds = defaultData.getShowAds();
                        AdminData.showVideoAd = defaultData.getVideoAds();
                        AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                        AdminData.contactEmail = defaultData.getContactEmail();
                        AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                        AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                        AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                        AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                        AdminData.videoCallsGems = defaultData.getVideoCalls();
                        AdminData.gemConversion = defaultData.getGemConversion();
                        AdminData.giftConversion = defaultData.getGiftConversion();
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public void querySubscriptions(BillingResult billingResult) {

        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
        if (billingClient == null ||
                purchasesResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            return;
        }
        onPurchasesUpdated(billingResult, purchasesResult.getPurchasesList());
    }

    private void initPermissionDialog() {
        dialogOverLayPermission = new DialogOverLayPermission();
        dialogOverLayPermission.setContext(this);
        dialogOverLayPermission.setCallBack(new OnOkClickListener() {
            @Override
            public void onOkClicked(Object o) {
                boolean isAllow = (boolean) o;
                if (isAllow) {
                    requestOverLayPermission();
                    miOverLayPermission();
                }
                dialogOverLayPermission.dismissAllowingStateLoss();
            }
        });
    }

    private void openPermissionDialog() {
//        if (SharedPref.getBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, true)) {
        if (dialogOverLayPermission != null && !dialogOverLayPermission.isAdded()) {
            dialogOverLayPermission.show(getSupportFragmentManager(), "PermissionDialog");
        }
//        }
    }

    private boolean checkOverLayPermission() {
        //Android M Or Over
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isOverLayEnabled = Settings.canDrawOverlays(this);
            if (isOverLayEnabled)
                SharedPref.putBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, false);
            return isOverLayEnabled;
        }

        /*  if (Build.VERSION.SDK_INT >= 19 && MIUIUtils.isMIUI() && !MIUIUtils.isFloatWindowOptionAllowed(getApplicationContext())){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean miOverLayEnabled = Settings.canDrawOverlays(this);
                if (miOverLayEnabled)
                    SharedPref.putBoolean(SharedPref.BACKGROUND_WINDOW_PERMISSION, false);
                return miOverLayEnabled;
            }
        }*/
        return true;
    }

    private void requestOverLayPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE_TECNO);
        }
    }

    public void miOverLayPermission() {
        if (AppUtils.getDeviceName().contains("Xiaomi")) {
            Log.i(TAG, "miOverLayPermission: " + AppUtils.getDeviceName());
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", getPackageName());
            startActivity(localIntent);
        }

        Log.i(TAG, "miOverLayPermissionn: " + AppUtils.getDeviceName());



       /* if (Build.VERSION.SDK_INT >= 19 && MIUIUtils.isMIUI() && !MIUIUtils.isFloatWindowOptionAllowed(getApplicationContext())) {
            Log.i(TAG, "MIUI DEVICE: Screen Overlay Not allowed");
            startActivityForResult(MIUIUtils.toFloatWindowPermission(getApplicationContext(), getPackageName()), REQUEST_OVERLAY_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getApplicationContext())) {
            Log.i(TAG, "SDK_INT > 23: Screen Overlay Not allowed");
            startActivityForResult(new Intent(
                            "android.settings.action.MANAGE_OVERLAY_PERMISSION",
                            Uri.parse("package:" +getPackageName()))
                    , Constants.REQUEST_OVERLAY_PERMISSION
            );
        } else {
            Log.i(TAG, "SKK_INT < 19 or Have overlay permission");

        }*/
    }


    private void enableAutoStart() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        sharedPrefs = getSharedPreferences("MyPref", 0);
        ;
        editor = sharedPrefs.edit();
        isAutoStartIn = sharedPrefs.getBoolean("isAutoStartIn", false);
        if (!isAutoStartIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_overlay_permission_autostart, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putBoolean("isAutoStartIn", true);
                    editor.apply();
                    Intent intent = new Intent();
                    // com.cyin.himgr.widget.activity.MainSettingGpActivity
                    intent.setClassName("com.transsion.phonemaster",
                            "com.cyin.himgr.widget.activity.MainActivity");
                    startActivity(intent);
                    alertDialog.dismiss();
                }
            });
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (!checkOverLayPermission()) {
                        openPermissionDialog();
//            miOverLayPermission();
                    }

                }
            });

            alertDialog.show();
/*
            new AlertDialog.Builder(MainActivity.this).setTitle(Build.MANUFACTURER + " Protected Apps")
                    .setMessage(
                            String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", this.getString(io.antmedia.android.R.string.app_name)))
                    .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor.putBoolean("isSettingsIn", true);
                            editor.commit();
                            Intent intent = new Intent();
                            // com.cyin.himgr.widget.activity.MainSettingGpActivity
                            intent.setClassName("com.transsion.phonemaster",
                                    "com.cyin.himgr.widget.activity.MainSettingGpActivity");
                            startActivity(intent);
                        }
                    })
                    .show();
*/
        }
    }


    private void enableProtectesApps() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        sharedPrefs = getSharedPreferences("MyPref", 0);
        editor1 = sharedPrefs.edit();
        isSettingsIn = sharedPrefs.getBoolean("isSettingsIn", false);
        if (!isSettingsIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_overlay_permission_call, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor1.putBoolean("isSettingsIn", true);
                    editor1.apply();
                    //Addons AudioVideo Call
                    isSettingsIn = true;
                    Intent intent = new Intent();
                    // com.cyin.himgr.widget.activity.MainSettingGpActivity
                    intent.setClassName("com.transsion.phonemaster",
                            "com.cyin.himgr.widget.activity.MainSettingGpActivity");
                    startActivity(intent);
                    alertDialog.dismiss();
                }
            });
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();

                    //Addons AudioVideoCall
                  /*  if (!checkOverLayPermission()) {
                        openPermissionDialog();
                    }*/

                }
            });

            alertDialog.show();

        }
    }


}
