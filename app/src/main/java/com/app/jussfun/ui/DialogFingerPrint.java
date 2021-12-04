package com.app.jussfun.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.utils.LottieValueAnimator;
import com.app.jussfun.R;
import com.app.jussfun.helper.FingerPrintCallBack;
import com.app.jussfun.helper.FingerprintUiHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DialogFingerPrint extends DialogFragment implements FingerprintUiHelper.Callback {

    private static final String TAG = DialogFingerPrint.class.getSimpleName();
    @BindView(R.id.iconFingerPrint)
    ImageView iconFingerPrint;
    @BindView(R.id.fingerprint_status)
    TextView fingerprintStatus;
    @BindView(R.id.txtAppName)
    TextView txtAppName;
    @BindView(R.id.fingerprint_container)
    RelativeLayout fingerprintContainer;
    @BindView(R.id.iconLock)
    LottieAnimationView iconLock;

    private FingerPrintCallBack fingerPrintCallBack;
    private FingerprintUiHelper mFingerprintUiHelper;
    private InputMethodManager mInputMethodManager;
    private Stage mStage = Stage.FINGERPRINT;
    private FingerprintManager.CryptoObject mCryptoObject;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInputMethodManager = context.getSystemService(InputMethodManager.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
//        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_finger_print, container, false);
        ButterKnife.bind(this, itemView);
        txtAppName.setText(getString(R.string.app_name) + " " + getString(R.string.app_locked));
        iconLock.playAnimation();
        iconLock.setRepeatCount(LottieValueAnimator.INFINITE);
        iconLock.setRepeatMode(LottieValueAnimator.RESTART);
        initView(itemView);

        return itemView;
    }

    private void initView(View v) {
        mFingerprintUiHelper = new FingerprintUiHelper(
                context.getSystemService(FingerprintManager.class),
                v.findViewById(R.id.iconFingerPrint),
                v.findViewById(R.id.fingerprint_status), v.findViewById(R.id.iconLock), this, context);

        if (!mFingerprintUiHelper.isFingerprintAuthAvailable()) {
            mFingerprintUiHelper.stopListening();

        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }


    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint {@code true} if the purchase was made by using a fingerprint
     * @param mCryptoObject   the Crypto object
     */
    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        fingerPrintCallBack.onPurchased(true /* withFingerprint */, mCryptoObject);
        dismiss();
    }

    @Override
    public void onError(String errString) {
        fingerPrintCallBack.onError(errString);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    public void setCallBack(FingerPrintCallBack fingerPrintCallBack) {
        this.fingerPrintCallBack = fingerPrintCallBack;
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }
}
