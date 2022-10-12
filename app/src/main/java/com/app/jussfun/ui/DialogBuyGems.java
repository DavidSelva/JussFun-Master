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
import android.widget.ImageView;
import android.widget.TextView;

import com.app.jussfun.R;
import com.app.jussfun.databinding.DialogFreeGemsBinding;
import com.app.jussfun.databinding.DialogFundsAlertBinding;
import com.app.jussfun.helper.callback.OnOkCancelClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogBuyGems extends DialogFragment {

    private static final String TAG = DialogBuyGems.class.getSimpleName();
    private DialogFundsAlertBinding binding;
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
        binding = DialogFundsAlertBinding.inflate(inflater, container, false);
        View itemView = binding.getRoot();
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        binding.btnNo.setText(context.getString(R.string.not_now));
        binding.btnOkay.setText(context.getString(R.string.buy_gems));
        binding.txtDescription.setVisibility(View.GONE);
        binding.txtTitle.setTextSize(24);
        binding.txtTitle.setText(R.string.insufficient_funds);

        binding.btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkCancelClickListener callBack) {
        this.callBack = callBack;
    }

    @OnClick({R.id.btnOkay, R.id.btnNo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnOkay:
                callBack.onOkClicked(null);
                break;
            case R.id.btnNo:
                callBack.onCancelClicked(null);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
