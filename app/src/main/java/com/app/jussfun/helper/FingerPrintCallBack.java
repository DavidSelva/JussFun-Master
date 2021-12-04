package com.app.jussfun.helper;

import android.hardware.fingerprint.FingerprintManager;

import androidx.annotation.Nullable;

public interface FingerPrintCallBack {
    void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject);

    void onError(String errString);
}
