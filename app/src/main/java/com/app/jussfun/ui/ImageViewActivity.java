package com.app.jussfun.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.app.jussfun.base.App;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.app.jussfun.external.ImagePicker;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.R;
import com.app.jussfun.utils.SharedPref;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.callback.OnSwipeTouchListener;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.InputStream;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ImageViewActivity extends BaseFragmentActivity {

    private static final String TAG = ImageViewActivity.class.getSimpleName();
    String image = "";
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.lottieImage)
    LottieAnimationView lottieImage;
    @BindView(R.id.shimmerLayout)
    ShimmerFrameLayout shimmerLayout;
    @BindView(R.id.bottomLay)
    LinearLayout bottomLay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    ApiInterface apiInterface;
    int resultCode = 0;
    @BindView(R.id.btnClose)
    ImageButton btnClose;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.txtChangePhoto)
    TextView txtChangePhoto;
    private RequestOptions profileImageRequest;
    StorageUtils storageUtils;
    String from, picString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);

        setContentView(R.layout.activity_image_view);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        image = getIntent().getStringExtra(Constants.TAG_USER_IMAGE);
        storageUtils = new StorageUtils(this);
        profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .dontAnimate();

        from = getIntent().getStringExtra(Constants.TAG_FROM);
        picString = getIntent().getStringExtra("DeletePic");
        if (from.equals(Constants.TAG_PROFILE)) {
            shimmerLayout.startShimmer();
            initListeners();
        } else {
            bottomLay.setVisibility(View.GONE);
        }

        switch (from) {
            case Constants.TAG_SENT:
                if (picString.equalsIgnoreCase("No")) {
                    Log.d(TAG, "onCreate: " + "First" + picString + storageUtils.getImage(from, image));
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        Glide.with(getApplicationContext())
                                .load(storageUtils.getImage(from, image))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        imageView.setImageDrawable(resource);
                                        return false;
                                    }
                                })
                                .into(imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(storageUtils.getImageUri(from, image))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        imageView.setImageDrawable(resource);
                                        return false;
                                    }
                                })
                                .into(imageView);
                    }
                } else {
                    Log.d(TAG, "onCreate: " + "Second" + Constants.CHAT_IMAGE_URL + image);
                    Glide.with(getApplicationContext())
                            .load(Constants.CHAT_IMAGE_URL + image)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imageView.setImageDrawable(resource);
                                    return false;
                                }
                            })
                            .into(imageView);
                }

                break;
            case Constants.TAG_RECEIVED:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                    Glide.with(getApplicationContext())
                            .load(storageUtils.getImage(from, image))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imageView.setImageDrawable(resource);
                                    return false;
                                }
                            })
                            .into(imageView);
                } else {
                    Glide.with(getApplicationContext())
                            .load(storageUtils.getImageUri(from, image))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    imageView.setImageDrawable(resource);
                                    return false;
                                }
                            })
                            .into(imageView);
                }

                break;
            // for addon gif
            case Constants.TAG_GIF:
                Glide
                        .with(getApplicationContext())
                        .asGif()
                        .load(image)
                        .into(imageView);
                break;
            default:
                Glide.with(getApplicationContext())
                        .load(Constants.IMAGE_URL + image)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                imageView.setImageDrawable(resource);
                                return false;
                            }
                        })
                        .apply(profileImageRequest)
                        .into(imageView);
                break;
        }

        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.TOP)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
    }

    private void initListeners() {
        parentLay.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeDown() {

            }

            @Override
            public void onSwipeLeft() {

            }

            @Override
            public void onSwipeUp() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(ImageViewActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{CAMERA}, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        ImagePicker.pickImage(ImageViewActivity.this);
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(ImageViewActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(ImageViewActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        ImagePicker.pickImage(ImageViewActivity.this);
                    }
                }
               /* if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(),
                                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{CAMERA,
                                    WRITE_EXTERNAL_STORAGE},
                            Constants.CAMERA_REQUEST_CODE);
                } else {
                    ImagePicker.pickImage(ImageViewActivity.this);
                }*/
            }

            @Override
            public void onSwipeRight() {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            try {
                InputStream imageStream = ImagePicker.getInputStreamFromResult(this, requestCode, resultCode, data);
                uploadImage(AppUtils.getBytes(imageStream));
                String filePath = ImagePicker.getImagePathFromResult(getApplicationContext(), requestCode, resultCode, data);
                Glide.with(getApplicationContext())
                        .load(filePath)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                imageView.setImageDrawable(resource);
                                return false;
                            }
                        })
                        .apply(profileImageRequest)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(byte[] imageBytes) {
        lottieImage.setVisibility(View.GONE);
        txtChangePhoto.setText(R.string.uploading);
        showLoading();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> data = response.body();
                    hideLoading();
                    resultCode = -1;
                    if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        GetSet.setUserImage(data.get(Constants.TAG_USER_IMAGE));
                        SharedPref.putString(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                        txtChangePhoto.setText(R.string.profile_image_updated);
                        //                    App.makeToast(getString(R.string.profile_image_updated));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onBackPressed();
                            }
                        }, 1000);
                    }
                } else {
                    App.makeToast(getString(R.string.something_went_wrong));
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                hideLoading();
                txtChangePhoto.setText(R.string.swipe_change_photo);
                lottieImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean checkPermissions(String[] permissionList, ImageViewActivity activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }


    private boolean shouldShowRationale(String[] permissions, ImageViewActivity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                if (checkPermissions(permissions, ImageViewActivity.this)) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    if (shouldShowRationale(permissions, ImageViewActivity.this)) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, permissions, 100);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.camera_storage_error), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(intent, 100);
                    }
                }


               /* boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
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
                            App.makeToast(getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }*/
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @OnClick(R.id.btnClose)
    public void onViewClicked() {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }
}
