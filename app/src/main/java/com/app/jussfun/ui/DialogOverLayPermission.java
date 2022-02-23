package com.app.jussfun.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.app.jussfun.R;
import com.app.jussfun.helper.callback.OnOkClickListener;
import com.app.jussfun.utils.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogOverLayPermission extends DialogFragment {


    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.txtDescription)
    AppCompatTextView txtDescription;
    @BindView(R.id.btnAllow)
    Button btnAllow;
    @BindView(R.id.btnDeny)
    Button btnDeny;
    @BindView(R.id.btnDontAsk)
    CheckBox btnDontAsk;
    private Context context;
    private OnOkClickListener callBack;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
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
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
        View itemView = inflater.inflate(R.layout.dialog_overlay_permission, container, false);
        ButterKnife.bind(this, itemView);

        btnDontAsk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_white));
                    btnAllow.setTextColor(context.getResources().getColor(R.color.textPrimary));
                    btnAllow.setEnabled(false);
                } else {
                    btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_primary));
                    btnAllow.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    btnAllow.setEnabled(true);
                }
            }
        });
        return itemView;

    }

    @OnClick({R.id.btnAllow, R.id.btnDeny})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btnAllow) {
            callBack.onOkClicked(true);
        } else if (view.getId() == R.id.btnDeny) {
            SharedPref.putBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, !btnDontAsk.isChecked());

//            SharedPref.putBoolean(SharedPref.BACKGROUND_WINDOW_PERMISSION, !btnDontAsk.isChecked());
            callBack.onOkClicked(false);
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }
}
