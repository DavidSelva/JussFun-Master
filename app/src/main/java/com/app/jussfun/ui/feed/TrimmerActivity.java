package com.app.jussfun.ui.feed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.databinding.ActivityTrimmerBinding;
import com.app.jussfun.external.videotrimmer.interfaces.OnHgLVideoListener;
import com.app.jussfun.external.videotrimmer.interfaces.OnTrimVideoListener;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrimmerActivity extends BaseActivity implements OnTrimVideoListener, OnHgLVideoListener {

    private static final String TAG = TrimmerActivity.class.getSimpleName();
    ActivityTrimmerBinding binding;
    private ProgressDialog mProgressDialog;
    private StorageUtils storageUtils;

    private SharedPreferences pref;
    String sourceType, beforeText;
    private int bottomNavHeight, bottomMargin = 0;
    private Uri videoUri;
    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        binding = ActivityTrimmerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pref = getSharedPreferences("SavedPref", MODE_PRIVATE);
        storageUtils = StorageUtils.getInstance(this);
        bottomMargin = AppUtils.dpToPx(this, 2);

        Intent extraIntent = getIntent();
        int maxDuration = 30;

        binding.bottomLay.setOnClickListener(null);

        binding.mainLay.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                bottomNavHeight = view.getPaddingBottom() + windowInsets.getSystemWindowInsetBottom() + bottomMargin;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initBottomPadding(bottomNavHeight + bottomMargin);
                    }
                }, 200);
                return windowInsets.consumeSystemWindowInsets();
            }
        });

        binding.inputLay.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                beforeText = "" + charSequence;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 250) {
                    App.makeToast(getString(R.string.maximum_characters_limit_reached));
                } else if ((binding.inputLay.edtMessage.getLineCount() > 6)) {
                    binding.inputLay.edtMessage.setText("" + beforeText);
                    binding.inputLay.edtMessage.setSelection(binding.inputLay.edtMessage.length() - 1);
                    App.makeToast(getString(R.string.maximum_characters_limit_reached));
                }
            }
        });

        if (Locale.getDefault().getLanguage().equals("ar")) {
            binding.inputLay.btnSend.setRotation(180);
        } else {
            binding.inputLay.btnSend.setRotation(0);
        }

        if (extraIntent != null) {
            videoUri = extraIntent.getData();
            sourceType = extraIntent.getStringExtra(Constants.TAG_FROM);
            maxDuration = extraIntent.getIntExtra("story_time", 30);
        }

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        if (binding.mVideoTrimmer != null) {
            /**
             * get total duration of video file
             */
            binding.mVideoTrimmer.setMaxDuration(maxDuration);
            binding.mVideoTrimmer.setOnTrimVideoListener(this);
            binding.mVideoTrimmer.setOnHgLVideoListener(this);
            binding.mVideoTrimmer.setVideoInformationVisibility(true);
            binding.mVideoTrimmer.setVideoURI(videoUri);
            binding.inputLay.btnSend.setOnClickListener(
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
                        @Override
                        public void onClick(View view) {
                            if (NetworkReceiver.isConnected()) {
                                binding.mVideoTrimmer.onSaveClicked();
                            } else {
                                AppUtils.showSnack(TrimmerActivity.this, findViewById(R.id.mainLay), false);
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    @Override
    public void onTrimStarted() {
        mProgressDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.mVideoTrimmer != null)
            binding.mVideoTrimmer.releasePlayer();
    }

    @Override
    protected void onStop() {
        if (binding.mVideoTrimmer != null)
            binding.mVideoTrimmer.releasePlayer();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initBottomPadding(int bottomPadding) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        binding.bottomLay.setPadding(0, 0, 0, bottomPadding);
        binding.bottomLay.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        mProgressDialog.cancel();
        binding.mVideoTrimmer.destroy();
        binding.mVideoTrimmer.stopPlayer();
        finish();
    }

    private void setToImmersiveMode() {
        // set to immersive
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void getResult(final Uri contentUri, Bitmap thumb, final long mDuration) {
        try {
            if (!NetworkReceiver.isConnected()) {
                mProgressDialog.cancel();
                AppUtils.showSnack(TrimmerActivity.this, findViewById(R.id.mainLay), false);
            } else {
                if (contentUri != null && contentUri.getPath() != null && !TextUtils.isEmpty(contentUri.getPath())) {
                    File previewFile = new File(contentUri.getPath());
                    Log.i(TAG, "getResult: " + previewFile.getAbsolutePath());
                    Log.i(TAG, "getResult: " + mDuration);
                    mProgressDialog.cancel();
                    if (NetworkReceiver.isConnected()) {
                        uploadImage(previewFile);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                    }
                    setResult(RESULT_OK);

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void uploadImage(File videoFile) {
        try {
            showLoading();
            InputStream imageStream = new FileInputStream(videoFile);
            RequestBody requestFile = RequestBody.create(storageUtils.getBytes(imageStream), MediaType.parse("video/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_FEED_IMAGE, videoFile.getName(), requestFile);
            RequestBody userId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
            Call<Map<String, String>> call = apiInterface.uploadImage(body, userId);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> data = response.body();
                        if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            addFeed(data.get(Constants.TAG_USER_IMAGE));
                        }
                    }
                }


                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e(TAG, "uploadImage: " + t.getMessage());
                    call.cancel();
                    hideLoading();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            hideLoading();
        }
    }

    private void addFeed(String fileUrl) {
        showLoading();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
        requestMap.put(Constants.TAG_FEED_IMAGE, fileUrl);
        requestMap.put(Constants.TAG_TITLE, "");
        requestMap.put(Constants.TAG_COMMENT_STATUS, binding.btnComment.isChecked() ? "" + 1 : "" + 0);
        requestMap.put(Constants.TAG_DESCRIPTION, !TextUtils.isEmpty(binding.inputLay.edtMessage.getText()) ? binding.inputLay.edtMessage.getText().toString() : "");

        Call<Map<String, String>> call = apiInterface.addFeed(requestMap);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                hideLoading();
                if (response.isSuccessful()) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                hideLoading();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK && requestCode == Constants.STATUS_VIDEO_CODE) {
//            setResult(Activity.RESULT_OK);
//            finish();
//        }
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        binding.mVideoTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(final String message) {
        mProgressDialog.cancel();
        if (message != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TrimmerActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onVideoPrepared() {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                 Toast.makeText(TrimmerActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void showLoading() {
        /*Disable touch options*/
        binding.progressLay.setVisibility(View.VISIBLE);
        binding.progressBar.setIndeterminate(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        binding.progressLay.setVisibility(View.GONE);
        binding.progressBar.setIndeterminate(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
