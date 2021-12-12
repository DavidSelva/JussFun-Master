package com.app.jussfun.utils;

import android.util.Log;

import com.app.jussfun.BuildConfig;

public class Logging {
    public static void e(String TAG, String errorMessage) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, errorMessage);
    }

    public static void d(String TAG, String debugMessage) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, debugMessage);
    }

    public static void v(String TAG, String verboseMessage) {
        if (BuildConfig.DEBUG) Log.v(TAG, verboseMessage);
    }

    public static void i(String TAG, String infoMessage) {
        if (BuildConfig.DEBUG) Log.i(TAG, infoMessage);
    }

    public static void w(String TAG, String warningMessage) {
        if (BuildConfig.DEBUG) Log.w(TAG, warningMessage);
    }
}
