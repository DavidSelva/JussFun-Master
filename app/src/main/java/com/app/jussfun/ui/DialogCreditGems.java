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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.app.jussfun.helper.OnOkCancelClickListener;
import com.app.jussfun.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogCreditGems extends DialogFragment {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.dimenImage)
    ImageView dimenImage;
    @BindView(R.id.txtDescription)
    TextView txtDescription;
    @BindView(R.id.btnOkay)
    Button btnOkay;
    @BindView(R.id.btnNo)
    Button btnNo;
    private Context context;
    private OnOkCancelClickListener callBack;
    private String message = "";

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_funds_alert, container, false);
        ButterKnife.bind(this, itemView);
        initView();
        return itemView;
    }

    private void initView() {
        txtTitle.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        txtDescription.setVisibility(View.VISIBLE);
        if (message.equals(getString(R.string.filter_error_message))) {
            txtDescription.setTextSize(18);
            Glide.with(context)
                    .load(R.drawable.avatar)
                    .apply(new RequestOptions().transform(new CircleCrop()))
                    .into(dimenImage);
            dimenImage.setVisibility(View.VISIBLE);
        } else if (message.equals(getString(R.string.no_user_message))) {
            Glide.with(context)
                    .load(R.drawable.avatar)
                    .apply(new RequestOptions().transform(new CircleCrop()))
                    .into(dimenImage);
            txtDescription.setVisibility(View.GONE);
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(message);
            dimenImage.setVisibility(View.VISIBLE);
        } else if (message.equals(getString(R.string.you_dont_have_any_gifts))) {
            txtDescription.setVisibility(View.GONE);
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(message);
            dimenImage.setImageDrawable(context.getDrawable(R.drawable.ic_gift_primary));
            dimenImage.setVisibility(View.VISIBLE);
        } else if (message.equals(getString(R.string.gems_purchase_response))) {
            txtDescription.setVisibility(View.GONE);
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(message);
            dimenImage.setVisibility(View.VISIBLE);
            dimenImage.setImageDrawable(context.getDrawable(R.drawable.diamond));
        } else {
            txtDescription.setTextSize(24);
        }
        txtDescription.setText(message);
        txtDescription.setTextColor(getResources().getColor(R.color.colorBlack));
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

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }
}
