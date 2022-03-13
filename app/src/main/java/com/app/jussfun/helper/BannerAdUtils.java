package com.app.jussfun.helper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.jussfun.utils.AdminData;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public class BannerAdUtils {
    private final static String TAG = BannerAdUtils.class.getSimpleName();
    private static BannerAdUtils mInstance = null;
    private Context mContext = null;

    public BannerAdUtils(Context mContext) {
        this.mContext = mContext;
    }

    public static BannerAdUtils getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new BannerAdUtils(mContext);
        }
        return mInstance;
    }


    public void loadAd(String TAG, AdView adView) {
        if (AdminData.isAdEnabled()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
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
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
        }
    }

    /*public void onResume(AdView adView) {
        if (adView != null) {
            adView
        }
    }*/

}
