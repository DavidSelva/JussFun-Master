package com.app.jussfun.helper;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdUtils {
    private final static String TAG = AdUtils.class.getSimpleName();
    private static AdUtils mInstance = null;
    private Context mContext = null;

    public AdUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static AdUtils getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new AdUtils(mContext);
        }
        return mInstance;
    }


    public void loadAd(String TAG, AdView adView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e(TAG, "onAdFailedToLoad: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
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
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }
}
