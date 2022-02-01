package com.app.jussfun.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.app.jussfun.base.App;
import com.app.jussfun.helper.AdUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.external.qrgenerator.QRGContents;
import com.app.jussfun.external.qrgenerator.QRGEncoder;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class QRCodeGenerateActivity extends BaseFragmentActivity {

    private static final String TAG = QRCodeGenerateActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.txtSubTitle)
    AppCompatTextView txtSubTitle;
    @BindView(R.id.appBar)
    Toolbar toolbar;
    @BindView(R.id.iconQRCode)
    ImageView iconQRCode;
    @BindView(R.id.btnDownload)
    Button btnDownload;
    @BindView(R.id.btnSettings)
    ImageView btnShare;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.qrLayout)
    CardView qrLayout;
    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;

    private QRGEncoder qrgEncoder;
    private String qrImagePath;
    private Bitmap qrBitMap;
    private StorageUtils storageUtils;
    private AppUtils appUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generate);
        ButterKnife.bind(this);
        appUtils = new AppUtils(this);
        storageUtils = new StorageUtils(this);
        initView();
        new GetQRCodeTask().execute();
    }

    private void initView() {
        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }
        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }
        btnShare.setVisibility(View.VISIBLE);
        btnShare.setImageDrawable(getDrawable(R.drawable.share));
        int padding = AppUtils.dpToPx(this, 12);
        btnShare.setPadding(padding, padding, padding, padding);
        txtTitle.setText(getString(R.string.my_qr_code));
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        /*RelativeLayout.LayoutParams qrLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        qrLayoutParams.width = AppUtils.dpToPx(getApplicationContext(), getDisplayWidth() / 3);
        qrLayoutParams.height = AppUtils.dpToPx(getApplicationContext(), 300);
        qrLayoutParams.addRule(Gravity.CENTER_HORIZONTAL);
        qrLayout.setLayoutParams(qrLayoutParams);*/

        loadAd();
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            AdUtils.getInstance(this).loadAd(TAG, adView);}
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnDownload, R.id.btnSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnDownload:
                Log.d(TAG, "onViewClicked: "+ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE));
                if (SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(QRCodeGenerateActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(QRCodeGenerateActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    Uri file = null;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        try {
                            if(storageUtils.saveToGallery(qrBitMap, Constants.TAG_IMAGE, getString(R.string.my_qr_code).concat(Constants.IMAGE_EXTENSION))){
                                App.makeToast(getString(R.string.saved_to_gallery));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        file = storageUtils.saveToSDCard(qrBitMap, Constants.TAG_IMAGE, getString(R.string.my_qr_code).concat(Constants.IMAGE_EXTENSION));
                        if (file != null) App.makeToast(getString(R.string.saved_to_gallery));

                    }

                }
                break;
            case R.id.btnSettings:
                shareQRCode();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == Constants.STORAGE_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: "+permissions);
            if (checkPermissions(permissions)) {
                Uri file = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        if (storageUtils.saveToGallery(qrBitMap, Constants.TAG_IMAGE, getString(R.string.my_qr_code).concat(Constants.IMAGE_EXTENSION))) {
                            App.makeToast(getString(R.string.saved_to_gallery));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file = storageUtils.saveToSDCard(qrBitMap, Constants.TAG_IMAGE, getString(R.string.my_qr_code).concat(Constants.IMAGE_EXTENSION));
                    if (file != null) App.makeToast(getString(R.string.saved_to_gallery));

                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermissions(permissions, Constants.STORAGE_REQUEST_CODE);
                    } else {
                        App.makeToast(getString(R.string.camera_storage_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }
            }
        }
    }

    public boolean checkPermissions(String[] permissions) {
        boolean permissionGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }
        return permissionGranted;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetQRCodeTask extends AsyncTask<Void, Void, String> {
        String profileData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.TAG_USER_ID, AppUtils.encryptMessage(GetSet.getUserId()));
                jsonObject.put(Constants.TAG_NAME, AppUtils.encryptMessage(GetSet.getName()));
                jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                jsonObject.put(Constants.TAG_AGE, Integer.parseInt(GetSet.getAge()));
                jsonObject.put(Constants.TAG_GENDER, GetSet.getGender());
                jsonObject.put(Constants.TAG_LOCATION, GetSet.getLocation());
                jsonObject.put(Constants.TAG_PREMIUM_MEBER, Boolean.parseBoolean(GetSet.getPremiumMember()));
                jsonObject.put(Constants.TAG_PRIVACY_CONTACT_ME, Boolean.parseBoolean(GetSet.getPrivacyContactMe()));
                jsonObject.put(Constants.TAG_PRIVACY_AGE, Boolean.parseBoolean(GetSet.getPrivacyAge()));

                jsonObject.put(Constants.TAG_APP_NAME,getString(R.string.app_name));

                profileData = "" + jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            profileData = new Gson().toJson(MainActivity.profileResponse);
//            profileData = AppUtils.encryptMessage(profileData);
        }

        @Override
        protected String doInBackground(Void... voids) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = Math.min(width, height);
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    profileData, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            Bitmap bitmap = qrgEncoder.getBitmap();
            if (bitmap != null) {
                File file = storageUtils.getTempFile(QRCodeGenerateActivity.this, System.currentTimeMillis() + ".jpg");
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    //getting the logo
                    Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_white);
                    overlay = Bitmap.createScaledBitmap(overlay, AppUtils.dpToPx(QRCodeGenerateActivity.this, 60),
                            AppUtils.dpToPx(QRCodeGenerateActivity.this, 60), false);
                    qrBitMap = mergeQRWithLogo(overlay, bitmap);
                    File tempFile = storageUtils.saveToCacheDir(qrBitMap, System.currentTimeMillis() + ".jpg");
                    return tempFile != null ? tempFile.getAbsolutePath() : null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String filePath) {
            super.onPostExecute(filePath);
            hideLoading();
            if (filePath != null) {
                Glide.with(getApplicationContext())
                        .load(filePath)
                        .into(iconQRCode);
                qrImagePath = filePath;
            } else {
                qrImagePath = null;
            }
        }
    }

    public Bitmap mergeQRWithLogo(Bitmap overlay, Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth - overlay.getWidth()) / 2;
        int centreY = (canvasHeight - overlay.getHeight()) / 2;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

    private void shareQRCode() {
        if (qrImagePath != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(QRCodeGenerateActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(qrImagePath));
            } else {
                uri = Uri.fromFile(new File(qrImagePath));
            }
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.checkout_this_profile_from) + " " + getString(R.string.app_name) + ": " +
                    "https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=" + LocaleManager.getLanguageCode(QRCodeGenerateActivity.this));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }

    public void showLoading() {
        /*Disable touch options*/
        progressBar.setVisibility(View.VISIBLE);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }

    public void hideLoading() {
        /*Enable touch options*/
        progressBar.setVisibility(View.GONE);
        /*getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }
}
