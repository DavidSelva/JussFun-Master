package com.app.jussfun.ui.feed;

import static com.hitasoft.app.helper.NetworkUtil.NOT_CONNECT;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.hitasoft.app.external.videotrimmer.HgLVideoTrimmer;
import com.hitasoft.app.external.videotrimmer.interfaces.OnHgLVideoListener;
import com.hitasoft.app.external.videotrimmer.interfaces.OnTrimVideoListener;
import com.hitasoft.app.helper.NetworkUtil;
import com.hitasoft.app.helper.StorageManager;
import com.hitasoft.app.helper.connectivity.NetworkStatus;
import com.hitasoft.app.hiddy.ApplicationClass;
import com.hitasoft.app.hiddy.BaseActivity;
import com.hitasoft.app.hiddy.NewGroupActivity;
import com.hitasoft.app.hiddy.R;
import com.hitasoft.app.utils.Constants;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class TrimmerActivity extends BaseActivity implements OnTrimVideoListener, OnHgLVideoListener {

    private static final String TAG = TrimmerActivity.class.getSimpleName();
    private HgLVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;
    private StorageManager storageManager;
    private EditText edtMessage;
    private ImageView ivEmoji;
    private RelativeLayout bottomLay, mainLay;
    private LinearLayout editLay;
    private ImageView btnSend;

    private SharedPreferences pref;
    private EmojiPopup mEmojiPopup;
    String filePath, sourceType, beforeText;
    private int bottomNavHeight, bottomMargin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setContentView(R.layout.activity_trimmer);
        pref = getSharedPreferences("SavedPref", MODE_PRIVATE);
        storageManager = StorageManager.getInstance(this);
        bottomMargin = ApplicationClass.dpToPx(this, 2);

        Intent extraIntent = getIntent();
        int maxDuration = 30;
        mVideoTrimmer = findViewById(R.id.timeLine);
        bottomLay = findViewById(R.id.bottom_lay);
        edtMessage = findViewById(R.id.edtMessage);
        ivEmoji = findViewById(R.id.iv_emoji);
        btnSend = findViewById(R.id.btnSend);
        mainLay = findViewById(R.id.mainLay);
        editLay = findViewById(R.id.editLay);

        bottomLay.setOnClickListener(null);
        setEmojiBuilder();

        mainLay.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
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

        edtMessage.addTextChangedListener(new TextWatcher() {
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
                    ApplicationClass.showToast(TrimmerActivity.this, getString(R.string.maximum_characters_limit_reached), Toast.LENGTH_SHORT);
                } else if ((edtMessage.getLineCount() > 6)) {
                    edtMessage.setText("" + beforeText);
                    edtMessage.setSelection(edtMessage.length() - 1);
                    ApplicationClass.showToast(TrimmerActivity.this, getString(R.string.maximum_characters_limit_reached), Toast.LENGTH_SHORT);
                }
            }
        });

        if (Locale.getDefault().getLanguage().equals("ar")) {
            btnSend.setRotation(180);
        } else {
            btnSend.setRotation(0);
        }

        if (extraIntent != null) {
            Log.d(TAG, "onCreateFilepath: " + filePath);
            filePath = extraIntent.getStringExtra(Constants.TAG_ATTACHMENT);
            sourceType = extraIntent.getStringExtra(Constants.TAG_FROM);
            maxDuration = extraIntent.getIntExtra("story_time", 30);
        }

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        if (mVideoTrimmer != null) {
            /**
             * get total duration of video file
             */
            mVideoTrimmer.setMaxDuration(maxDuration);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnHgLVideoListener(this);
            //mVideoTrimmer.setDestinationPath("/storage/emulated/0/DCIM/CameraCustom/");
            mVideoTrimmer.setVideoInformationVisibility(true);
            mVideoTrimmer.setVideoURI(Uri.parse(filePath));
            btnSend.setOnClickListener(
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
                        @Override
                        public void onClick(View view) {
                            if (NetworkStatus.isConnected()) {
                                mVideoTrimmer.onSaveClicked();
                            } else {
                                ApplicationClass.showSnack(TrimmerActivity.this, findViewById(R.id.mainLay), false);
                            }
                        }
                    }
            );
        }
    }

    public void setEmojiBuilder() {
        mEmojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.parentLay))
                .setOnEmojiPopupShownListener(() -> {
                    ivEmoji.setImageResource(R.drawable.ic_baseline_keyboard_24);
                })
                .setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(int height) {
                        if (height > 0) {
                            if (bottomLay != null)
                                bottomLay.setTranslationY(-height);
                        } else {
                            if (bottomLay != null)
                                bottomLay.setTranslationY(0);
                        }
                    }
                })
                .setOnEmojiPopupDismissListener(() -> {
                    ivEmoji.setImageResource(R.drawable.ic_emoji);
                })
                .setOnSoftKeyboardCloseListener(() -> {
                    ivEmoji.setImageResource(R.drawable.ic_emoji);
                    if (bottomLay != null)
                        bottomLay.setTranslationY(0);
                })
                .build(edtMessage);

        ivEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmojiPopup.toggle();
            }
        });
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.mainLay), isConnected);
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
        if (mVideoTrimmer != null)
            mVideoTrimmer.releasePlayer();
    }

    @Override
    protected void onStop() {
        if (mVideoTrimmer != null)
            mVideoTrimmer.releasePlayer();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initBottomPadding(int bottomPadding) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomLay.setPadding(0, 0, 0, bottomPadding);
        bottomLay.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        if (mEmojiPopup != null && mEmojiPopup.isShowing()) {
            mEmojiPopup.dismiss();
            //            ApplicationClass.hideSoftKeyboard(this);
        } else {
            mProgressDialog.cancel();
            mVideoTrimmer.destroy();
            mVideoTrimmer.stopPlayer();
            finish();
        }
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
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                mProgressDialog.cancel();
                ApplicationClass.showSnack(TrimmerActivity.this, findViewById(R.id.mainLay), false);
            } else {
                if (contentUri != null && contentUri.getPath() != null && !TextUtils.isEmpty(contentUri.getPath())) {
                    File file = new File(contentUri.getPath());
                    String previewFilePath = file.getAbsolutePath();
                    Log.i(TAG, "getResult: " + previewFilePath);
                    Log.i(TAG, "getResult: " + mDuration);
                    mProgressDialog.cancel();
                    HashMap<String, String> map = new HashMap<>();
                    map.put(Constants.TAG_MESSAGE, "" + edtMessage.getText().toString());
                    map.put(Constants.TAG_ATTACHMENT, previewFilePath);
                    map.put(Constants.TAG_TYPE, "video");
                    Intent intent = new Intent(this, NewGroupActivity.class);
                    intent.putExtra(Constants.TAG_FROM, StorageManager.TAG_STATUS);
                    intent.putExtra(Constants.TAG_SOURCE_TYPE, sourceType);
                    intent.putExtra(Constants.TAG_MESSAGE_DATA, map);
                    if (thumb != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        thumb.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        thumb.recycle();
                        intent.putExtra(Constants.TAG_DATA, byteArray);
                        intent.putExtra(Constants.TAG_TIME_STAMP, mDuration);
                    }
                    startActivityForResult(intent, Constants.STATUS_VIDEO_CODE);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(TrimmerActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.STATUS_VIDEO_CODE) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
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
}
