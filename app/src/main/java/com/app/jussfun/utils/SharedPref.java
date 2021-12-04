package com.app.jussfun.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Belal on 03/11/16.
 */

public class SharedPref {
    private static final String SHARED_PREF_NAME = "RandooPref";
    private static SharedPref mInstance;
    public static SharedPreferences mPref;
    public static SharedPreferences.Editor mEditor;
    private static Context mContext;

    public static final String USER_ID = "userId";
    public static final String IS_LOGGED = "isLogged";
    public static final String NAME = "name";
    public static final String USER_NAME = "userName";
    public static final String LOGIN_ID = "loginId";
    public static final String LOGIN_TYPE = "loginType";
    public static final String USER_IMAGE = "userImage";
    public static final String LOCATION = "location";
    public static final String FOLLOWERS_COUNT = "followersCount";
    public static final String FOLLOWINGS_COUNT = "followingsCount";
    public static final String FACEBOOK_EMAIL = "facebook_email";
    public static final String FACEBOOK_NAME = "facebook_name";
    public static final String FACEBOOK_IMAGE = "facebook_image";
    public static final String GIFTS = "gifts";
    public static final String VIDEOS = "videos";
    public static final String EMAIL = "email";
    public static final String GENDER = "gender";
    public static final String DOB = "dob";
    public static final String AGE = "age";
    public static final String GEMS = "gems";
    public static final String AUTH_TOKEN = "authToken";
    public static final String IS_PREMIUM_MEMBER = "isPremiumMember";
    public static final String PREMIUM_EXPIRY = "premiumExpiry";
    public static final String PRIVACY_AGE = "privacyAge";
    public static final String PRIVACY_CONTACT_ME = "privacyContactMe";
    public static final String SHOW_NOTIFICATION = "showNotification";
    public static final String CHAT_NOTIFICATION = "chatNotification";
    public static final String FOLLOW_NOTIFICATION = "followNotification";
    public static final String INTEREST_NOTIFICATION = "interestNotification";
    public static final String ONCE_PAID = "oncePaid";
    public static final String IN_APP_PRICE = "in_app_price";
    public static final String IN_APP_VALIDITY = "in_app_validity";
    public static final String GIFT_EARNINGS = "gift_earnings";
    public static final String REFERAL_LINK = "referal_link";
    public static final String REFERAL_CODE = "referal_code";
    public static final String VIDEO_END_TIME = "video_end_time";
    public static final String CREATED_AT = "created_at";
    public static final String DEFAULT_SUBS_SKU = "default_subs_sku";
    public static final String IS_FINGERPRINT_LOCKED = "finger_print_locked";
    public static final String FRIENDS_COUNT = "friends_count";
    public static final String INTEREST_COUNT = "interest_notification";
    public static final String UNLOCKS_LEFT = "unlocks_left";
    public static final String IS_APP_OPENED = "isAppOpened";


    public static final String CHAT_LANGUAGE = "chat_language";
    public static final String DEFAULT_CHAT_LANGUAGE = "";
    public static final String PAYPAL_ID = "paypal_id";

    public static final String GIFT_CONVERSION_EARNINGS = "gift_earnings";
    public static final String GIFT_CONVERSION_VALUE = "gift_earnings";

    public static final String STREAM_BASE_URL = "stream_base_url";
    public static final String STREAM_WEBSOCKET_URL = "stream_websocket_url";
    public static final String STREAM_VOD_URL = "stream_vod_url";
    public static final String STREAM_API_URL = "stream_api_url";


    public static final String POP_UP_WINDOW_PERMISSION = "popup_window_permission";

    public static final String BACKGROUND_WINDOW_PERMISSION = "background_window_permission";

    private SharedPref() {
    }

    public static void initPref(Context mContext) {
        mPref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        SharedPref.mContext = mContext;
    }

    public static SharedPreferences getInstance(Context mContext) {
        mPref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        return mPref;
    }

    public static SharedPreferences.Editor getEditor(Context mContext) {
        mPref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        return mEditor;
    }
    /*Put methods*/

    public static void putString(String key, String val) {
        mEditor.putString(key, val).commit();
    }

    public static void putInt(String key, int val) {
        mEditor.putInt(key, val).commit();
    }

    public static void putFloat(String key, float val) {
        mEditor.putFloat(key, val).commit();
    }

    public static void putLong(String key, long val) {
        mEditor.putLong(key, val).commit();
    }

    public static void putBoolean(String key, Boolean val) {
        mEditor.putBoolean(key, val).commit();
    }
    /*Get methods*/

    public static String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return mPref.getInt(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue) {
        return mPref.getFloat(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return mPref.getLong(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return mPref.getBoolean(key, defaultValue);
    }

    public static void clearAll() {
        mEditor.clear();
        mEditor.commit();
    }

}
