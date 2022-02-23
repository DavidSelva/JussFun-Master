package com.app.jussfun.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.app.jussfun.base.App;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.external.ImagePicker;
import com.app.jussfun.helper.callback.OnOkClickListener;
import com.app.jussfun.R;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DialogProfileImage extends DialogFragment {

    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.profileImage)
    ImageView profileImage;
    @BindView(R.id.btnFinish)
    Button btnFinish;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.headerLay)
    RelativeLayout headerLay;
    private OnOkClickListener callBack;
    private Context context;
    InputStream imageStream;

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
        View itemView = inflater.inflate(R.layout.dialog_profile_image, container, false);
        ButterKnife.bind(this, itemView);
        progressBar.setIndeterminate(true);

        if (SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null) != null) {
            Glide.with(context)
                    .load(SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null))
                    .apply(RequestOptions.circleCropTransform())
                    .apply(App.getProfileImageRequest())
                    .into(profileImage);
        }
        return itemView;

    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }

    @OnClick({R.id.profileLayout, R.id.btnFinish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profileLayout:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(context, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }
                }else{
                    if (ContextCompat.checkSelfPermission(context, CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }
                }
                break;
            case R.id.btnFinish:
                App.preventMultipleClick(btnFinish);
                callBack.onOkClicked(imageStream);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 234) {
            try {
                profileImage.setVisibility(View.VISIBLE);
                imageStream = ImagePicker.getInputStreamFromResult(context, requestCode, resultCode, data);
                String filePath = ImagePicker.getImagePathFromResult(context, requestCode, resultCode, data);
                GetSet.setUserImage(filePath);
                Glide.with(context)
                        .load(filePath)
                        .apply(App.getProfileImageRequest())
                        .into(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (permissionGranted) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        } else {
                            App.makeToast(context.getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
        }
    }


    public void setContext(Context context) {
        this.context = context;
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnFinish.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnFinish.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
