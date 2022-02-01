package com.app.jussfun.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.app.jussfun.ui.RandomCallActivity;
import com.app.jussfun.ui.RandomWebSocket;
import com.app.jussfun.ui.SplashActivity;
import com.app.jussfun.ui.VideoCallActivity;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.stetho.Stetho;
import com.app.jussfun.R;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.helper.ThemeHelper;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class App extends android.app.Application implements LifecycleObserver, CameraXConfig.Provider {
    private static final String TAG = App.class.getSimpleName();
    private static App instance;
    private static Activity mCurrentActivity = null;
    private Locale locale;
    public static boolean onShareExternal = false, onAppForegrounded = false;
    public static RequestOptions profileImageRequest;
    private StorageUtils storageUtils;

    private ApiInterface apiInterface;

    public static String from;

    public static void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = sharedPreferences.getString("themePref", ThemeHelper.DEFAULT_MODE);
        ThemeHelper.applyTheme(themePref);

        storageUtils = new StorageUtils(this);
        SharedPref.initPref(instance);
        Stetho.initializeWithDefaults(this);
        SharedPref.putBoolean(SharedPref.IS_APP_OPENED, false);

        profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .dontAnimate()
                .circleCrop();

        Log.i(TAG, "onAppForegroundedonCreate: " + isOnAppForegrounded());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AppUtils.resetFilter();
        if (SharedPref.getBoolean(SharedPref.IS_LOGGED, false)) {
            AppWebSocket.getInstance(this);
            GetSet.setLogged(true);
            GetSet.setUserId(SharedPref.getString(SharedPref.USER_ID, null));
            GetSet.setLoginId(SharedPref.getString(SharedPref.LOGIN_ID, null));
            GetSet.setLoginType(SharedPref.getString(SharedPref.LOGIN_TYPE, null));
            GetSet.setAuthToken(SharedPref.getString(SharedPref.AUTH_TOKEN, null));
            GetSet.setName(SharedPref.getString(SharedPref.NAME, null));
            GetSet.setUserImage(SharedPref.getString(SharedPref.USER_IMAGE, null));
            GetSet.setAge(SharedPref.getString(SharedPref.AGE, null));
            GetSet.setDob(SharedPref.getString(SharedPref.DOB, null));
            GetSet.setGender(SharedPref.getString(SharedPref.GENDER, null));
            GetSet.setLocation(SharedPref.getString(SharedPref.LOCATION, null));
            GetSet.setGems(SharedPref.getLong(SharedPref.GEMS, 0L));
            GetSet.setGifts(SharedPref.getLong(SharedPref.GIFTS, 0L));
            GetSet.setVideos(SharedPref.getLong(SharedPref.VIDEOS, 0L));
            GetSet.setFollowersCount(SharedPref.getString(SharedPref.FOLLOWERS_COUNT, "0"));
            GetSet.setFollowingCount(SharedPref.getString(SharedPref.FOLLOWINGS_COUNT, "0"));
            GetSet.setFriendsCount(SharedPref.getInt(SharedPref.FRIENDS_COUNT, 0));
            GetSet.setInterestsCount(SharedPref.getInt(SharedPref.INTEREST_COUNT, 0));
            GetSet.setUnlocksLeft(SharedPref.getInt(SharedPref.UNLOCKS_LEFT, 0));
            GetSet.setPremiumMember(SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, Constants.TAG_FALSE));
            GetSet.setPremiumExpiry(SharedPref.getString(SharedPref.PREMIUM_EXPIRY, Constants.TAG_FALSE));
            GetSet.setPrivacyAge(SharedPref.getString(SharedPref.PRIVACY_AGE, Constants.TAG_FALSE));
            GetSet.setPrivacyContactMe(SharedPref.getString(SharedPref.PRIVACY_CONTACT_ME, Constants.TAG_FALSE));
            GetSet.setShowNotification(SharedPref.getString(SharedPref.SHOW_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setChatNotification(SharedPref.getString(SharedPref.CHAT_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setFollowNotification(SharedPref.getString(SharedPref.FOLLOW_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setInterestNotification(SharedPref.getBoolean(SharedPref.INTEREST_NOTIFICATION, false));
            GetSet.setGiftEarnings(SharedPref.getString(SharedPref.GIFT_EARNINGS, "0"));
            GetSet.setReferalLink(SharedPref.getString(SharedPref.REFERAL_LINK, ""));
            GetSet.setCreatedAt(SharedPref.getString(SharedPref.CREATED_AT, ""));
            if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                GetSet.setOncePurchased(true);
            } else {
                GetSet.setOncePurchased(SharedPref.getBoolean(SharedPref.ONCE_PAID, false));
            }
        }

    }

    public static synchronized App getInstance() {
        return instance;
    }

    public static void makeToast(String message) {
        Toast.makeText(instance.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void makeCustomToast(String message) {
        Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        toast.getView()
                .setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message))
                .setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toast.show();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        fix();
    }

    public static void fix() {
        try {
            Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
            Method method = clazz.getSuperclass().getDeclaredMethod("stop");
            method.setAccessible(true);

            Field field = clazz.getDeclaredField("INSTANCE");
            field.setAccessible(true);

            method.invoke(field.get(null));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        onAppForegrounded = true;
        getAppDefaultData();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!(App.getCurrentActivity() instanceof SplashActivity)) {
                    AppWebSocket.mInstance = null;
                    AppWebSocket.getInstance(instance);
                }

                if (App.getCurrentActivity() instanceof RandomCallActivity) {
                    RandomWebSocket.mInstance = null;
                    RandomWebSocket.getInstance(instance);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (RandomCallActivity.isInCall && !VideoCallActivity.isInCall) {
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                    jsonObject.put(Constants.TAG_PARTNER_ID, RandomCallActivity.partnerId);
                                    jsonObject.put(Constants.TAG_MSG_TYPE, Constants.ONLINE);
                                    Log.i(TAG, "TAG_NOTIFY_USER: " + jsonObject);
                                    RandomWebSocket.getInstance(instance).send(jsonObject.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, 1000);
                } else if ((App.getCurrentActivity() instanceof VideoCallActivity)) {
                    if (NetworkReceiver.isConnected()) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (VideoCallActivity.isInCall && !RandomCallActivity.isInCall && AppWebSocket.getInstance(instance) != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                                        jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                        jsonObject.put(Constants.TAG_RECEIVER_ID, VideoCallActivity.receiverId.equals(GetSet.getUserId())
                                                ? VideoCallActivity.userId : VideoCallActivity.receiverId);
                                        jsonObject.put(Constants.TAG_MSG_TYPE, Constants.ONLINE);
                                        Log.i(TAG, "TAG_NOTIFY_USER: " + jsonObject);
                                        AppWebSocket.getInstance(instance).send(jsonObject.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, 1000);
                    }
                } else {
               /* if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false)) {
                    DialogAppLock dialogAppLock = new DialogAppLock();
                    dialogAppLock.setCallBack(new FingerPrintCallBack() {
                        @Override
                        public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {

                        }

                        @Override
                        public void onError(String errString) {

                        }
                    });
                    dialogAppLock.setCancelable(false);
                    dialogAppLock.show(get());
                }*/
                }
            }
        }, 500);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        onAppForegrounded = false;
        Log.i(TAG, "onAppBackgrounded: " + onAppForegrounded);
        if (GetSet.isIsLogged() && AppWebSocket.getInstance(this) != null) {
            if (VideoCallActivity.isInCall) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_RECEIVER_ID, VideoCallActivity.receiverId.equals(GetSet.getUserId())
                            ? VideoCallActivity.userId : VideoCallActivity.receiverId);
                    jsonObject.put(Constants.TAG_MSG_TYPE, Constants.OFFLINE);
                    Log.i(TAG, "TAG_NOTIFY_USER: " + jsonObject);
                    AppWebSocket.getInstance(this).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            AppWebSocket.getInstance(instance).disconnect();
        }

        if (GetSet.isIsLogged() && RandomWebSocket.getInstance(instance) != null) {
            if (RandomCallActivity.isInCall) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_PARTNER_ID, RandomCallActivity.partnerId);
                    jsonObject.put(Constants.TAG_MSG_TYPE, Constants.OFFLINE);
                    Log.i(TAG, "TAG_NOTIFY_USER: " + jsonObject);
                    RandomWebSocket.getInstance(instance).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onAppDestroy() {

    }

    public static boolean isOnAppForegrounded() {
        return onAppForegrounded;
    }

    public void changeLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        String lang = SharedPref
                .getString(Constants.TAG_LANGUAGE_CODE, Constants.DEFAULT_LANGUAGE_CODE);
        if (!(config.locale.getLanguage().equals(lang))) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            Logging.i(TAG, "changeLocale: " + lang);
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }

    public static void preventMultipleClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 1000);
    }

    public static RequestOptions getProfileImageRequest() {
        return profileImageRequest;
    }

    public static int getScreenOrientation(Activity context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int orientation;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        orientation = context.getResources().getConfiguration().orientation;
        return orientation;
    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    if (response.isSuccessful()) {
                        AppDefaultResponse defaultData = response.body();
                        if (defaultData.getStatus().equals(Constants.TAG_TRUE)) {
                            AdminData.filterOptions = defaultData.getFilterOptions();
                            AdminData.gemConversion = defaultData.getGemConversion();
                            AdminData.giftConversion = defaultData.getGiftConversion();
                        }
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    Log.i(TAG, "getAppDefaultDataonFailure: " + t.getMessage());
                }
            });
        }
    }

    // addon for interstitial advertisements

/*
    public static void loadAds(Context context) {
        Log.i(TAG, "loadAds: "+"loadAd");
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(InterstitialAd_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("2B125C77FEDAAAACF9E05411F4E1BDFE").build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "onAdLoaded: ");
                if (mInterstitialAd!=null){
                    mInterstitialAd.show();
                }
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("TAG", "FailedToLoad: " + errorCode);

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                Log.i(TAG, "onAdCloseds: "+"onAdClosed");



            }
        });



    }
*/

}
