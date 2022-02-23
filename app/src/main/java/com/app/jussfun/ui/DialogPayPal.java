package com.app.jussfun.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;


import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.callback.OnOkCancelClickListener;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogPayPal extends DialogFragment {

    private static final String TAG = DialogPayPal.class.getSimpleName();
    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.edtPayPal)
    AppCompatEditText edtPayPal;
    @BindView(R.id.btnConvert)
    Button btnConvert;
    @BindView(R.id.btnCancel)
    Button btnCancel;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private Context context;
    private OnOkCancelClickListener callBack;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }

        ProfileResponse response=new ProfileResponse();
        if(response.getPaypalId()!=null){
            edtPayPal.setText(response.getPaypalId());
        }

     /*   ProfileResponse request = new ProfileResponse();
        if (request.getPayPalId()!=null){
            edtPayPal.setText(request.getPayPalId());
            Log.v("check","tezt"+request.getPayPalId());
        }*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_paypal, container, false);
        ButterKnife.bind(this, itemView);
        initView();
        return itemView;
    }

    private void initView() {
        ProfileResponse request = new ProfileResponse();
        Log.v("Text","message"+request.getPaypalId());
        progressBar.setIndeterminate(true);

        if (SharedPref.getString(SharedPref.PAYPAL_ID, null) != null) {
            edtPayPal.setText(SharedPref.getString(SharedPref.PAYPAL_ID, null));
        }






    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkCancelClickListener callBack) {
        this.callBack = callBack;
    }

    @OnClick({R.id.btnConvert, R.id.btnCancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnConvert:
                if (TextUtils.isEmpty(btnConvert.getText())) {
                    App.makeToast("Enter valid paypal id");
                }

                if (SharedPref.getString(SharedPref.PAYPAL_ID, null) != null) {
                    edtPayPal.setText(SharedPref.getString(SharedPref.PAYPAL_ID, null));
                }
                else if(edtPayPal.getText().toString().trim().length()==0){
                    App.makeToast("Enter a paypal id");
                }

                else {
                    callBack.onOkClicked("" + edtPayPal.getText());
                }
                Log.i(TAG, "onViewClickeddia: "+callBack);
                break;
            case R.id.btnCancel:
                callBack.onCancelClicked(null);
                break;
        }
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnConvert.setEnabled(false);
        btnCancel.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnConvert.setEnabled(true);
        btnCancel.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
