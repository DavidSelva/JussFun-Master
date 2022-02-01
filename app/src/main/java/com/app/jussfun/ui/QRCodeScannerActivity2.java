package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.app.jussfun.base.App;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.external.ImagePicker;
import com.app.jussfun.external.qrscannner.QrCodeFinderView;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.Logging;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.telephony.MbmsDownloadSession.RESULT_CANCELLED;

public class QRCodeScannerActivity2 extends BaseFragmentActivity {

    private static final String TAG = QRCodeScannerActivity2.class.getSimpleName();

    StorageUtils storageUtils;
    AppUtils appUtils;
    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;
    @BindView(R.id.previewView)
    PreviewView previewView;
    @BindView(R.id.qrCodeViewFinder)
    QrCodeFinderView qrCodeViewFinder;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.btnFlash)
    CheckBox btnFlash;
    @BindView(R.id.btnMyQRCode)
    ImageView btnMyQRCode;
    @BindView(R.id.btnGallery)
    ImageView btnGallery;
    private ExecutorService cameraExecutor;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private int displayId;
    private Preview preview = null;
    private Camera camera = null;
    private CameraSelector cameraSelector;
    DisplayMetrics displayMetrics;

    String imageFrom="";
    boolean galleryFrom = false;

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {

        @Override
        public void onDisplayAdded(int i) {

        }

        @Override
        public void onDisplayRemoved(int i) {

        }

        @Override
        public void onDisplayChanged(int id) {
            if (displayId == id) {
//                Log.d(TAG, "Rotation changed: " + id);
//                imageCapture.setTargetRotation(display.rotation);
//                imageAnalyzer.setTargetRotation(display.rotation);
            }
            ;
        }

    };
    private boolean mSquareViewFinder = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);
        ButterKnife.bind(this);
        appUtils = new AppUtils(this);
        storageUtils = new StorageUtils(this);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        initView();
    }

    private void initView() {
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.scan));
        btnBack.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
    }

    private void initCameraView() {
        // Wait for the views to be properly laid out
        previewView.post(new Runnable() {
            @Override
            public void run() {
                // Bind use cases
                bindCameraUseCases();
            }
        });
    }

    private void bindCameraUseCases() {
        // Get screen metrics used to setup camera for full screen resolution
        int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int rotation = previewView.getDisplay().getRotation();
        // Bind the CameraProvider to the LifeCycleOwner
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                // CameraProvider
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    // Preview
                    preview = new Preview.Builder()
                            // We request aspect ratio but no resolution
                            .setTargetAspectRatio(screenAspectRatio)
                            // Set initial target rotation
                            .setTargetRotation(rotation)
                            .build();

                    // Attach the viewfinder's surface provider to preview use case
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                    // The analyzer can then be assigned to the instance
                    // Setup image analysis pipeline that computes average pixel luminance
                    // ImageAnalysis
                    ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                            // We request aspect ratio but no resolution
                            .setTargetAspectRatio(screenAspectRatio)
                            // Set initial target rotation, we will have to call this again if rotation changes
                            // during the lifecycle of this use case
                            .setTargetRotation(rotation)
                            .build();
                    imageAnalyzer.setAnalyzer(cameraExecutor, new ZxingQrCodeAnalyzer());
                    // Must unbind the use-cases before rebinding them
                    cameraProvider.unbindAll();

                    try {
                        // A variable number of use-cases can be passed here -
                        // camera provides access to CameraControl & CameraInfo
                        camera = cameraProvider.bindToLifecycle((LifecycleOwner) QRCodeScannerActivity2.this, cameraSelector, imageAnalyzer, preview);
                        camera.getCameraControl().enableTorch(btnFlash.isChecked());
                        qrCodeViewFinder.setVisibility(View.VISIBLE);
                    } catch (Exception exception) {
                        Logging.e(TAG, "Use case binding failed: " + exception.getMessage());
                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Method used to re-draw the camera UI controls, called every time configuration changes.
     */
    private void updateCameraUi() {
        // Remove previous UI if any
//        parentLay.removeView(this);

        // In the background, load latest photo taken (if any) for gallery thumbnail
        // We can only change the foreground Drawable using API level 23+ API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Display flash animation to indicate that photo was captured
            parentLay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parentLay.setForeground(new ColorDrawable(Color.WHITE));
                    parentLay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            parentLay.setForeground(null);
                        }
                    }, Constants.ANIMATION_FAST_MILLIS);
                }
            }, Constants.ANIMATION_SLOW_MILLIS);
        }
    }

    /**
     * [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     * [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     * <p>
     * Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     * of preview ratio to one of the provided values.
     *
     * @param width  - preview width
     * @param height - preview height
     * @return suitable aspect ratio
     */
    private int aspectRatio(int width, int height) {
        int previewRatio = Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - AspectRatio.RATIO_4_3) <= Math.abs(previewRatio - AspectRatio.RATIO_16_9)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     * <p>
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateCameraUi();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                if (!isPermissionsGranted(permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        } else {
//                            openPermissionDialog(permissions);
                            App.makeToast(getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                } else {
                    initCameraView();
                }
                break;
        }
    }

    public boolean isPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, Constants.CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraExecutor = Executors.newSingleThreadExecutor();
        if (checkCameraPermission()) {
            initCameraView();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Shut down our background executor
        cameraExecutor.shutdown();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnFlash, R.id.btnGallery, R.id.btnMyQRCode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnFlash:
                camera.getCameraControl().enableTorch(btnFlash.isChecked());
                break;
            case R.id.btnMyQRCode: {
                Intent intent = new Intent(getApplicationContext(), QRCodeGenerateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;
            case R.id.btnGallery:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(QRCodeScannerActivity2.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(QRCodeScannerActivity2.this, new String[]{CAMERA}, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }
                }else{
                    if (ContextCompat.checkSelfPermission(QRCodeScannerActivity2.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(QRCodeScannerActivity2.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(QRCodeScannerActivity2.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = ImagePicker.getImageFromResult(getApplicationContext(), requestCode, resultCode, data);
                int[] bytes = new int[bitmap.getWidth() * bitmap.getHeight()];
                bitmap.getPixels(bytes, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                        bitmap.getHeight());
                LuminanceSource source = buildLuminanceSource(bytes, bitmap);
                galleryFrom = true;
                scanQRFromSource(source, null);
            } else if (resultCode == RESULT_CANCELLED) {
                //handle cancel
            }
        }
    }

    private class ZxingQrCodeAnalyzer implements ImageAnalysis.Analyzer {
        private long lastAnalyzedTimestamp = 0L;

        /**
         * Helper extension function used to extract a byte array from an
         * image plane buffer
         */

        @Override
        public void analyze(@NonNull ImageProxy image) {
            long currentTimestamp = System.currentTimeMillis();
            // Calculate the average luma no more often than every second
            if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
                // Since format in ImageAnalysis is YUV, image.planes[0]
                // contains the Y (luminance) plane
                try {
                    // ImageProxy uses an ImageReader under the hood:
                    // https://developer.android.com/reference/androidx/camera/core/ImageProxy.html
                    // That has a default format of YUV_420_888 if not changed.
                    // https://developer.android.com/reference/android/graphics/ImageFormat.html#YUV_420_888
                    // https://developer.android.com/reference/android/media/ImageReader.html
                    if ((image.getFormat() == ImageFormat.YUV_420_888
                            || image.getFormat() == ImageFormat.YUV_422_888
                            || image.getFormat() == ImageFormat.YUV_444_888)
                            && image.getPlanes().length == 3) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        // Create a LuminanceSource.
                        Result rawResult = null;
                        LuminanceSource source = buildLuminanceSource(bytes, image);
                        scanQRFromSource(source, image);
                    } else {
                        // Manage other image formats
                        // TODO - https://developer.android.com/reference/android/media/Image.html
                    }
                } catch (IllegalStateException ise) {
                    ise.printStackTrace();
                }
                lastAnalyzedTimestamp = currentTimestamp;
            }
            image.close();
        }

        private LuminanceSource buildLuminanceSource(byte[] bytes, ImageProxy image) {
            LuminanceSource source = new PlanarYUVLuminanceSource(bytes,
                    image.getWidth(),
                    image.getHeight(),
                    0,
                    0,
                    image.getWidth(),
                    image.getHeight(),
                    false);
            return source;

            /*//            Rect rect = qrCodeViewFinder.getFrameRect();
            int left = 0;
            int top = (image.getHeight() - AppUtils.dpToPx(getApplicationContext(), 150)) / 2;
            int right = Math.min(image.getWidth(), qrCodeViewFinder.getFrameRect().width());
            int bottom = top + AppUtils.dpToPx(getApplicationContext(), 150);
            Rect rect = new Rect(left, top - AppUtils.dpToPx(getApplicationContext(), 20), right,
                    bottom - AppUtils.dpToPx(getApplicationContext(), 80));

            Log.i(TAG, "buildLuminanceSource: " + rect.left);
            Log.i(TAG, "buildLuminanceSource: " + rect.top);

            LuminanceSource crop = source.crop(rect.left, rect.top, rect.width(), rect.height());
            return crop;*/
        }
    }

    private void scanQRFromSource(LuminanceSource source, ImageProxy image) {
        Result rawResult = null;
        MultiFormatReader reader = null;
        // Create a Binarizer
        HybridBinarizer binarizer = new HybridBinarizer(source);
        // Create a BinaryBitmap.
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        try {
            // Try decoding...
            reader = new MultiFormatReader();
            reader.setHints(changeZXingDecodeDataMode());
            rawResult = reader.decodeWithState(binaryBitmap);
            Result finalRawResult = rawResult;
            if (finalRawResult != null)
            QRCodeScannerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProfileResponse profileResponse = new ProfileResponse();
                    try {
                        // Vibrate for 500 milliseconds
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (v != null) {
                                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            }
                        } else {
                            //deprecated in API 26
                            if (v != null) {
                                v.vibrate(500);
                            }
                        }
                        JSONObject jsonObject = new JSONObject(finalRawResult.getText());
                        if (jsonObject.has(Constants.TAG_PRIVACY_CONTACT_ME)) {
                            if (image != null) image.close();
                            profileResponse.setUserId(AppUtils.decryptMessage(jsonObject.getString(Constants.TAG_USER_ID)));
                            profileResponse.setName(AppUtils.decryptMessage(jsonObject.getString(Constants.TAG_NAME)));
                            profileResponse.setUserImage(jsonObject.getString(Constants.TAG_USER_IMAGE));
                            profileResponse.setAge(jsonObject.getString(Constants.TAG_AGE));
                            profileResponse.setGender(jsonObject.getString(Constants.TAG_GENDER));
                            profileResponse.setLocation(jsonObject.getString(Constants.TAG_LOCATION));
                            profileResponse.setPremiumMember(jsonObject.getString(Constants.TAG_PREMIUM_MEBER));
                            profileResponse.setPrivacyContactMe(jsonObject.getString(Constants.TAG_PRIVACY_CONTACT_ME));
                            profileResponse.setPrivacyAge(jsonObject.getString(Constants.TAG_PRIVACY_AGE));

                            profileResponse.setApp_name(jsonObject.getString(Constants.TAG_APP_NAME));
                            Log.i(TAG, "QRCodeScanru: "+jsonObject);
                            if (jsonObject.getString(Constants.TAG_APP_NAME).equals(getString(R.string.app_name))) {
                                if (!profileResponse.getUserId().equals(GetSet.getUserId())) {
                                    Intent profile = new Intent(getApplicationContext(), OthersProfileActivity.class);
                                    profile.putExtra(Constants.TAG_PARTNER_ID, profileResponse.getUserId());
                                    profile.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                                    profile.putExtra(Constants.TAG_FROM, Constants.TAG_QR_CODE);
                                    profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(profile);
                                } else {
                                    Intent profile = new Intent(getApplicationContext(), MyProfileActivity.class);
                                    profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    profile.putExtra(Constants.TAG_PARTNER_ID, profileResponse.getUserId());
                                    profile.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                                    profile.putExtra(Constants.TAG_FROM, Constants.TAG_QR_CODE);
                                    startActivity(profile);
                                }
                            }else {
                                App.makeToast(getString(R.string.invalid_qr_code));
                            }
                        } else {
                            App.makeToast(getString(R.string.invalid_qr_code));
                        }
                    } catch (JSONException e) {
                        App.makeToast(getString(R.string.invalid_qr_code));
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            if (image == null) Log.e(TAG, "scanQRFromSource: " + e.getMessage());
            if (galleryFrom){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        App.makeToast(getString(R.string.invalid_qr_code));
                        galleryFrom = false;
                    }
                });
                finish();
            }
            if (e.getMessage() != null)
                Log.e(TAG, "scanQRFromSource " + e.getMessage());
        } finally {
            if (reader != null) reader.reset();
        }
    }

    private LuminanceSource buildLuminanceSource(int[] bytes, Bitmap image) {
        LuminanceSource source = new RGBLuminanceSource(
                image.getWidth(),
                image.getHeight(),
                bytes);
        return source;
    }

    private Map<DecodeHintType, Object> changeZXingDecodeDataMode() {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>(EnumSet.of(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        return hints;
    }
}
