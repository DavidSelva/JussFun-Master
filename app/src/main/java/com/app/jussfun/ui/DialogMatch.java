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

import com.app.jussfun.R;
import com.app.jussfun.helper.OnOkClickListener;
import com.app.jussfun.model.FollowersResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogMatch extends DialogFragment {
    private static final String TAG = DialogMatch.class.getSimpleName();
    @BindView(R.id.bgImage)
    ImageView bgImage;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnChat)
    Button btnChat;
    @BindView(R.id.txtLikeDes)
    TextView txtLikeDes;
    @BindView(R.id.userImage)
    ImageView userImage;
    @BindView(R.id.partnerImage)
    ImageView partnerImage;
    private Context context;
    private OnOkClickListener callBack;
    private FollowersResponse.FollowersList partnerData;

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
        View itemView = inflater.inflate(R.layout.dialog_match, container, false);
        ButterKnife.bind(this, itemView);
        initView(itemView);
        return itemView;

    }

    private void initView(View itemView) {
        Glide.with(context)
                .load(Constants.IMAGE_URL + GetSet.getUserImage())
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(userImage);

        Glide.with(context)
                .load(Constants.IMAGE_URL + partnerData.getUserImage())
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(partnerImage);

        btnBack.setVisibility(View.VISIBLE);
        btnBack.setColorFilter(context.getResources().getColor(R.color.colorWhite));
        txtLikeDes.setText(String.format(getString(R.string.like_each_other), partnerData.getName()));
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @OnClick({R.id.btnBack, R.id.btnChat})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.btnBack) {
            callBack.onOkClicked(false);
        } else if (view.getId() == R.id.btnChat) {
            callBack.onOkClicked(true);
        }
    }

    public void setPartnerData(FollowersResponse.FollowersList partnerData) {
        this.partnerData = partnerData;
    }
}
