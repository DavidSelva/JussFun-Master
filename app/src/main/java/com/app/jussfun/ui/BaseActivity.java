package com.app.jussfun.ui;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.app.jussfun.helper.AppWebSocket;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.R;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;

public abstract class BaseActivity extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    NetworkReceiver networkReceiver;
    private int displayHeight;
    private int displayWidth;
    boolean previousState = NetworkReceiver.isConnected();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.anim_slide_left_in, R.anim.anim_stay);
        SharedPref.initPref(this);
        networkReceiver = new NetworkReceiver();
        networkReceiver.setConnectivityListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        Log.d(TAG, "attachBaseContext");
    }

   /* @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.setCurrentActivity(this);
    }

    @Override
    protected void onStop() {
        clearReferences();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    public void registerNetworkReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);
    }

    public void unregisterNetworkReceiver() {
        try {
            if (networkReceiver != null) {
                unregisterReceiver(networkReceiver);
            }
        } catch (Exception e) {
            Logging.e(TAG, "unregisterNetworkReceiver: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected && GetSet.isIsLogged()) {
            AppWebSocket.getInstance(this);
        } else {
            if (AppWebSocket.getInstance(this)!= null) {
                AppWebSocket.getInstance(this).disconnect();
            }
            AppWebSocket.mInstance = null;
        }
        if (previousState != isConnected) {
            previousState = isConnected;
            onNetworkChanged(isConnected);
        }
    }

    public abstract void onNetworkChanged(boolean isConnected);

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            super.setRequestedOrientation(requestedOrientation);
        } catch (IllegalStateException e) {
            // Only fullscreen activities can request orientation
            Logging.e(TAG, "setRequestedOrientation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearReferences() {
        Activity currActivity = App.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            App.setCurrentActivity(null);
    }
}
