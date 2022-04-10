package com.app.jussfun.ui;


import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.callback.FingerPrintCallBack;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.AddDeviceRequest;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.DeviceTokenPref;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseFragmentActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private ApiInterface apiInterface;
    DBHelper dbHelper;
    ImageView splashImage;
    private BiometricPrompt.PromptInfo promptInfo;
    private ExecutorService executor;
    private BiometricPrompt biometricPrompt;
    private AppUpdateManager appUpdateManager;
    InstallStateUpdatedListener listener;
    private boolean isMandatoryUpdate;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    Cipher defaultCipher;
    Cipher cipherNotInvalidated;
    private AppUtils appUtils;
    private boolean isFromFeed = false;
    private String feedId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_splash);
        SharedPref.putBoolean(SharedPref.IS_APP_OPENED, false);
        isMandatoryUpdate = false;
        appUtils = new AppUtils(this);
        StorageUtils storageUtils = new StorageUtils(this);
        storageUtils.deleteCacheDir();
        executor = Executors.newSingleThreadExecutor();
        appUtils.hideKeyboard(this);
        initView();
        new GetAppDefaultTask().execute();
        // start service for observing intents
//        startService(new Intent(this, LockscreenService.class));
        printHashKey();
//        checkFingerPrintEnabled();
        checkUser();
        checkIsFromLink();
    }

    private void initView() {
        //   splashImage = findViewById(R.id.splashImage);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        AppUtils.callerList = new ArrayList<>();
        dbHelper = DBHelper.getInstance(this);
        //   splashImage.setImageDrawable(getDrawable(R.mipmap.ic_splash));
    }

    private void checkForUpdate() {
        int appUpdateType = isMandatoryUpdate ? AppUpdateType.IMMEDIATE : AppUpdateType.FLEXIBLE;
        int requestCode = isMandatoryUpdate ? Constants.REQUEST_APP_UPDATE_IMMEDIATE : Constants.REQUEST_APP_UPDATE_FLEXIBLE;
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        listener = state -> {
            Log.i(TAG, "checkForUpdate: " + state.installStatus());
            // (Optional) Provide a download progress bar.
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                long bytesDownloaded = state.bytesDownloaded();
                long totalBytesToDownload = state.totalBytesToDownload();
                // Implement progress bar.

            } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                // When status updates are no longer needed, unregister the listener.
                appUpdateManager.completeUpdate();
                appUpdateManager.unregisterListener(listener);
                popupSnackbarForCompleteUpdate();
            }
        };

        // Returns an intent object that you use to check for an update
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            Log.i(TAG, "checkForUpdate: " + appUpdateInfo.updateAvailability());
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateType == AppUpdateType.IMMEDIATE && appUpdateInfo.isUpdateTypeAllowed(appUpdateType)) {
                    // Request the update.
                    startAppUpdateImmediate(appUpdateInfo);
                } else if (appUpdateType == AppUpdateType.FLEXIBLE && appUpdateInfo.isUpdateTypeAllowed(appUpdateType)) {
                    // Request the update.
                    appUpdateManager.registerListener(listener);
                    startAppUpdateFlexible(appUpdateInfo);
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                checkUser();
            } else {
                checkUser();
            }
        }).addOnFailureListener(e -> {
                    Log.e(TAG, "checkForUpdate: " + e.getMessage());
//                    checkUser();
                }
        );
    }

    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    Constants.REQUEST_APP_UPDATE_IMMEDIATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    Constants.REQUEST_APP_UPDATE_FLEXIBLE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        if (appUpdateManager != null) {
            appUpdateManager
                    .getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> {
                                //FLEXIBLE:
                                // If the update is downloaded but not installed,
                                // notify the user to complete the update.
                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                    popupSnackbarForCompleteUpdate();
                                }

                                //IMMEDIATE:
                                if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    // If an in-app update is already running, resume the update.
                                    startAppUpdateImmediate(appUpdateInfo);
                                }
                            }).addOnFailureListener(new com.google.android.play.core.tasks.OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    checkUser();
                }
            });
        }

    }

    private void checkUser() {
        SharedPref.putBoolean(SharedPref.IS_APP_OPENED, true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!VideoCallActivity.isInCall)
                            checkUserIsLoggedIn();
                    }
                }, 2000);
            }
        });
    }

    private void checkUserIsLoggedIn() {
       /* if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!checkOverLayPermission()) {
                requestCameraPermission();
            }
        }*/
        if (SharedPref.getBoolean("isLogged", false)) {
            if (!AppUtils.callerList.contains(GetSet.getUserId())) {
                AppUtils.callerList.add(GetSet.getUserId());
            }
            dbHelper.updateChatsOnline();
            dbHelper.updateChatsTyping();
            GetSet.setLogged(true);
            GetSet.setUserId(SharedPref.getString(SharedPref.USER_ID, null));
            GetSet.setLoginId(SharedPref.getString(SharedPref.LOGIN_ID, null));
            GetSet.setAuthToken(SharedPref.getString(SharedPref.AUTH_TOKEN, null));
            GetSet.setName(SharedPref.getString(SharedPref.NAME, null));
            GetSet.setUserImage(SharedPref.getString(SharedPref.USER_IMAGE, null));
            GetSet.setAge(SharedPref.getString(SharedPref.AGE, null));
            GetSet.setDob(SharedPref.getString(SharedPref.DOB, null));
            GetSet.setGender(SharedPref.getString(SharedPref.GENDER, null));
            GetSet.setLocation(SharedPref.getString(SharedPref.LOCATION, null));
            GetSet.setGems(SharedPref.getLong(SharedPref.GEMS, 0));
            GetSet.setGifts(SharedPref.getLong(SharedPref.GIFTS, 0));
            GetSet.setVideos(SharedPref.getLong(SharedPref.VIDEOS, 0));
            GetSet.setFollowersCount(SharedPref.getString(SharedPref.FOLLOWERS_COUNT, null));
            GetSet.setFollowingCount(SharedPref.getString(SharedPref.FOLLOWINGS_COUNT, null));
            GetSet.setInterestsCount(SharedPref.getInt(SharedPref.INTEREST_COUNT, 0));
            GetSet.setFriendsCount(SharedPref.getInt(SharedPref.FRIENDS_COUNT, 0));
            GetSet.setUnlocksLeft(SharedPref.getInt(SharedPref.UNLOCKS_LEFT, 0));
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            if (isFromFeed) {
                intent.putExtra(Constants.TAG_FEED_ID, feedId);
            }
            startActivity(intent);
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(SplashActivity.this);
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        SplashActivity.this.finish();
    }

    private boolean checkOverLayPermission() {
        //Android M Or Over
        return (!Settings.canDrawOverlays(this));
    }

    private void requestCameraPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE);
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
                    checkUser();
                }
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
            checkForUpdate();
        }
    }

    private void checkIsFromLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        Log.d(TAG, "onSuccess: "+ deepLink);
                        if (deepLink != null) {
                            if (deepLink.getQueryParameter("referal_code") != null) {
                                String referalCode = deepLink.getQueryParameter("referal_code");
                                SharedPref.putString(SharedPref.REFERAL_CODE, referalCode);
                            } else if (deepLink.getQueryParameter("feed_id") != null) {
                                isFromFeed = true;
                                feedId = deepLink.getQueryParameter("feed_id");
                            } else {
                                SharedPref.putString(SharedPref.REFERAL_CODE, "");
                            }
                        } else {
                            SharedPref.putString(SharedPref.REFERAL_CODE, "");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logging.e(TAG, "onSuccess: " + e.getMessage());
            }
        });
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
                finishAndRemoveTask();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkUserIsLoggedIn();
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
            checkUser();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initCipher(defaultCipher, Constants.DEFAULT_KEY_NAME);
            }
            DialogFingerPrint dialogFingerPrint = new DialogFingerPrint();
            dialogFingerPrint.setContext(this);
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
                    checkUserIsLoggedIn();
                }
            });
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            App.makeToast("Failed to encrypt the data with the generated key. " + "Retry the purchase");
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNewAppVersionState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterInstallStateUpdListener();
    }

    /* Displays the snackbar notification and call to action. */
    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.splashLay),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.colorPrimary));
        snackbar.show();
        unregisterInstallStateUpdListener();
    }


    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    if (response.isSuccessful()) {
                        AppDefaultResponse defaultData = response.body();
                        if (("" + defaultData.getStatus()).equals(Constants.TAG_TRUE)) {
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
                            AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                            AdminData.showVideoAd = defaultData.getVideoAds();
                            AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                            AdminData.contactEmail = defaultData.getContactEmail();
                            AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                            AdminData.videoCallsGems = defaultData.getVideoCalls();
                            if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
                                SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, AdminData.membershipList.get(0).getSubsTitle());
                            } else {
                                SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU);
                            }
                            AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                            AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                            AdminData.gemConversion = defaultData.getGemConversion();
                            AdminData.giftConversion = defaultData.getGiftConversion();
                            AdminData.feedTitle = defaultData.getFeedTitle();
                            AdminData.feedDescription = defaultData.getFeedDescription();
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    App.makeToast(getString(R.string.something_went_wrong));
                    call.cancel();
                }
            });
        }
    }

    private void addDeviceID() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

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

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Logging.d(TAG, "printHashKey: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Logging.e(TAG, "printHashKey" + e.getMessage());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAppDefaultTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getAppDefaultData();
            if (SharedPref.getBoolean("isLogged", false)) {
                addDeviceID();
            }
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEVICE_LOCK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkForUpdate();
            } else {
                App.makeToast(getString(R.string.unable_to_verify));
                finish();
            }
        } else if (requestCode == Constants.REQUEST_APP_UPDATE_IMMEDIATE || requestCode == Constants.REQUEST_APP_UPDATE_FLEXIBLE) {
            if (resultCode != RESULT_OK) {
                Logging.d(TAG, "Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
                unregisterInstallStateUpdListener();
                if (requestCode == Constants.REQUEST_APP_UPDATE_FLEXIBLE && resultCode == RESULT_CANCELED) {
                    checkUser();
                } else {
                    if (!VideoCallActivity.isInCall)
                        checkUserIsLoggedIn();
                }
            } else {
                if (!VideoCallActivity.isInCall)
                    checkUserIsLoggedIn();
            }
        } else {
            if (!VideoCallActivity.isInCall)
                checkUserIsLoggedIn();

        }
    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && listener != null)
            appUpdateManager.unregisterListener(listener);
    }
}
