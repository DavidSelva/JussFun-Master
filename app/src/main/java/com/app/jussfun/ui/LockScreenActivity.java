package com.app.jussfun.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.FingerPrintCallBack;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;
import com.app.jussfun.utils.SharedPref;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class LockScreenActivity extends BaseFragmentActivity {
    private static final String TAG = LockScreenActivity.class.getSimpleName();
    private FingerPrintCallBack callBack;
    private BiometricPrompt.PromptInfo promptInfo;
    private ExecutorService executor;
    private BiometricPrompt biometricPrompt;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    Cipher defaultCipher;
    Cipher cipherNotInvalidated;
    private AppUtils appUtils;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_app_lock);
        context = this;
        appUtils = new AppUtils(context);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void checkFingerPrintEnabled() {
        if (appUtils.checkIsDeviceEnabled(context)) {
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

            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    openBioMetricDialog();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openFingerPrintDialog();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
                if (fingerprintManager != null) {
                    if (fingerprintManager.hasEnrolledFingerprints()) {
                        createKey(Constants.DEFAULT_KEY_NAME, true);
                        createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                        openFingerPrintDialog();
                    } else if (km != null && km.isKeyguardSecure()) {
                        Intent i = km.createConfirmDeviceCredentialIntent(getString(R.string.authentication_required), getString(R.string.password));
                        startActivityForResult(i, Constants.DEVICE_LOCK_REQUEST_CODE);
                    } else {
//                        checkFromNotification();
                    }
                }
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
//            checkFromNotification();
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
//                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

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
        FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
        if (fingerprintManager != null && !fingerprintManager.hasEnrolledFingerprints()) {
//            checkFromNotification();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initCipher(defaultCipher, Constants.DEFAULT_KEY_NAME);
            }
            DialogFingerPrint dialogFingerPrint = new DialogFingerPrint();
            dialogFingerPrint.setContext(context);
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
//            checkFromNotification();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            App.makeToast("Failed to encrypt the data with the generated key. " + "Retry the purchase");
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

}
