package com.app.jussfun.ui;

import static android.telephony.MbmsDownloadSession.RESULT_CANCELLED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import com.app.jussfun.base.App;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener;
import com.google.android.exoplayer2.util.Log;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.external.ImagePicker;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.callback.OnOkCancelClickListener;
import com.app.jussfun.helper.PermissionsUtils;
import com.app.jussfun.helper.PointsOverlayView;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class QRCodeScannerActivity extends AppCompatActivity
    implements ActivityCompat.OnRequestPermissionsResultCallback, OnQRCodeReadListener , View.OnClickListener{

  private static final int MY_PERMISSION_REQUEST_CAMERA = 0;

  private static final String TAG = QRCodeScannerActivity.class.getSimpleName();

  private ViewGroup mainLayout;
  private ImageView btnGallery, btnBack, btnMyQRCode;
  private TextView resultTextView;
  private AppCompatTextView txtTitle;
  private QRCodeReaderView qrCodeReaderView;
  private CheckBox flashlightCheckBox;
  private CheckBox enableDecodingCheckBox;
  private PointsOverlayView pointsOverlayView;

  private ActivityResultLauncher<String[]> storagePermissionResult;
  private ActivityResultLauncher<Intent> v11StoragePermissionResult;

  boolean isStart= false;
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_decoder);

    if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    isStart = false;
    mainLayout = (ViewGroup) findViewById(R.id.main_layout);


    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) {
      initQRCodeReaderView();
    } else {
      requestCameraPermission();
    }

    initPermission();


  }

  @Override protected void onResume() {
    super.onResume();

   if(isStart){
    qrCodeReaderView.setQRDecodingEnabled(true);
    qrCodeReaderView.surfaceCreated(qrCodeReaderView.getHolder());
   }
    Log.e("checkOnresume"," start  "+isStart+ " qrcode "+qrCodeReaderView );
    isStart = false;

    if (qrCodeReaderView != null) {
      qrCodeReaderView.startCamera();
    }
  }

  @Override protected void onPause() {
    super.onPause();

    if (qrCodeReaderView != null) {
      qrCodeReaderView.stopCamera();
    }
    isStart = true;
  }


  @SuppressLint("MissingSuperCall")
  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                   @NonNull int[] grantResults) {
    if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
      return;
    }

    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Snackbar.make(mainLayout, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show();
      initQRCodeReaderView();
    } else {
      Snackbar.make(mainLayout, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
          .show();
    }
  }

  // Called when a QR is decoded
  // "text" : the text encoded in QR
  // "points" : points where QR control points are placed
  @Override public void onQRCodeRead(String text, PointF[] points) {
    //resultTextView.setText(text);
    Log.e("resultData","- "+text);
    pointsOverlayView.setPoints(points);

    QRCodeScannerActivity.this.runOnUiThread(new Runnable() {
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
          JSONObject jsonObject = new JSONObject(text);
          if (jsonObject.has(Constants.TAG_PRIVACY_CONTACT_ME)) {


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
            android.util.Log.i(TAG, "QRCodeScanru: "+jsonObject);

            if (jsonObject.getString(Constants.TAG_APP_NAME).equals(getString(R.string.app_name))) {
              isStart = true;
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
  }

  private void requestCameraPermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
      Snackbar.make(mainLayout, "Camera access is required to display the camera preview.",
          Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
        @Override public void onClick(View view) {
          ActivityCompat.requestPermissions(QRCodeScannerActivity.this, new String[] {
              Manifest.permission.CAMERA
          }, MY_PERMISSION_REQUEST_CAMERA);
        }
      }).show();
    } else {
      Snackbar.make(mainLayout, "Permission is not available. Requesting camera permission.",
          Snackbar.LENGTH_SHORT).show();
      ActivityCompat.requestPermissions(this, new String[] {
          Manifest.permission.CAMERA
      }, MY_PERMISSION_REQUEST_CAMERA);
    }
  }

  private void initQRCodeReaderView() {
    View content = getLayoutInflater().inflate(R.layout.content_decoder, mainLayout, true);

    qrCodeReaderView = (QRCodeReaderView) content.findViewById(R.id.qrdecoderview);
    resultTextView = (TextView) content.findViewById(R.id.result_text_view);
    flashlightCheckBox = (CheckBox) content.findViewById(R.id.flashlight_checkbox);
    enableDecodingCheckBox = (CheckBox) content.findViewById(R.id.enable_decoding_checkbox);
    pointsOverlayView = (PointsOverlayView) content.findViewById(R.id.points_overlay_view);
    btnGallery = (ImageView)content. findViewById(R.id.btnGallery);
    btnBack = (ImageView)content. findViewById(R.id.btnBack);
    btnMyQRCode = (ImageView)content. findViewById(R.id.btnMyQRCode);
    txtTitle = (AppCompatTextView) content. findViewById(R.id.txtTitle);

    txtTitle.setVisibility(View.VISIBLE);
    txtTitle.setText(getString(R.string.scan));
    btnBack.setVisibility(View.VISIBLE);
    if (LocaleManager.isRTL()) {
      btnBack.setScaleX(-1);
    } else {
      btnBack.setScaleX(1);
    }

    qrCodeReaderView.setAutofocusInterval(2000L);
    qrCodeReaderView.setOnQRCodeReadListener(this);
    qrCodeReaderView.setBackCamera();


    btnGallery.setOnClickListener(this);
    btnMyQRCode.setOnClickListener(this);
    btnBack.setOnClickListener(this);


    flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        qrCodeReaderView.setTorchEnabled(isChecked);
      }
    });
    enableDecodingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        qrCodeReaderView.setQRDecodingEnabled(isChecked);
      }
    });

      qrCodeReaderView.startCamera();

  }

  @Override
  public void onClick(View v) {
   switch(v.getId()){
     case R.id.btnGallery:
       if (PermissionsUtils.checkStoragePermission(QRCodeScannerActivity.this)) {
         ImagePicker.pickImage(QRCodeScannerActivity.this, true);
       } else {
         requestStoragePermissions();
       }
       break;
     case R.id.btnMyQRCode:
       Intent intent = new Intent(getApplicationContext(), QRCodeGenerateActivity.class);
       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
       startActivity(intent);
       break;
     case R.id.btnBack:
       finish();
       break;
   }
  }
  private void requestStoragePermissions() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      storagePermissionResult.launch(PermissionsUtils.READ_PERMISSIONS);
    }else{
      storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
    }
    //}
  }


  private void initPermission() {
    storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
      @Override
      public void onActivityResult(Map<String, Boolean> result) {
        boolean granted = true;
        android.util.Log.d(TAG, "onActivityResult: " + result);
        for (Map.Entry<String, Boolean> x : result.entrySet()) {
          if (!x.getValue()) granted = false;
        }

        if (granted) {
          btnGallery.performClick();
        } else {
          android.util.Log.d(TAG, "requestPermissions: ");
          for (Map.Entry<String, Boolean> x : result.entrySet()) {
            if (!x.getValue()) {
              if (ActivityCompat.shouldShowRequestPermissionRationale(QRCodeScannerActivity.this, x.getKey())) {
                storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
              } else {
                PermissionsUtils.openPermissionDialog(QRCodeScannerActivity.this, new OnOkCancelClickListener() {
                  @Override
                  public void onOkClicked(Object o) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                  }

                  @Override
                  public void onCancelClicked(Object o) {

                  }
                }, getString(R.string.storage_permission_error));
              }
              break;
            }
          }
        }

      }
    });

    v11StoragePermissionResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
              @Override
              public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                  // There are no request codes
                  Intent data = result.getData();
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                      btnGallery.performClick();
                    } else {
                      App.makeToast(getString(R.string.storage_permission_error));
                    }
                  }
                }
              }
            });
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
        scanQRFromSource(source, null);
      } else if (resultCode == RESULT_CANCELLED) {
        //handle cancel
      }
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


  private void scanQRFromSource(LuminanceSource source, ImageProxy image) {
    Result rawResult = null;
    //Toast.makeText(getApplicationContext(),"OtherProfileActivity",Toast.LENGTH_SHORT).show();
    MultiFormatReader reader = null;
    // Create a Binarizer
    HybridBinarizer binarizer = new HybridBinarizer(source);
    // Create a BinaryBitmap.
    BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
    try {
      android.util.Log.i(TAG, "qrscananalyzescanQRFromSour: " + reader);
      android.util.Log.i(TAG, "qrscananalyzescanQRFromSourc: " + rawResult);
      // Try decoding...

      reader = new MultiFormatReader();
      reader.setHints(changeZXingDecodeDataMode());
      rawResult = reader.decodeWithState(binaryBitmap);
      android.util.Log.i(TAG, "qrscananalyzescanQRFromSource: " + rawResult);
      Result finalRawResult = rawResult;

      android.util.Log.i(TAG, "qrscananalyzescanQRFromSourcee: " + finalRawResult.getText());
      if (finalRawResult != null)
        android.util.Log.i(TAG, "scanQRFromSource: " + finalRawResult.getText());
      QRCodeScannerActivity.this.runOnUiThread(new Runnable() {
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
              android.util.Log.i(TAG, "QRCodeScanru: "+jsonObject);
              if (jsonObject.getString(Constants.TAG_APP_NAME).equals(getString(R.string.app_name))) {
                isStart = true;
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
      android.util.Log.i(TAG, "qrscananalyzescanQRFromSource1: " + source);
    } catch (Exception e) {
      if (image == null) {
        android.util.Log.e(TAG, "scanQRFromSource: " + e.getMessage());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            App.makeToast(getString(R.string.invalid_qr_code));
          }
        });

      }
      if (e.getMessage() != null)
        android.util.Log.e(TAG, "scanQRFromSource " + e.getMessage());
    } finally {
      if (reader != null) reader.reset();
    }
  }


}