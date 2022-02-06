package com.app.jussfun.ui.feed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.external.SpacesItemDecoration;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class FilterActivity extends BaseActivity implements ThumbnailsAdapter.ThumbnailsAdapterListener, View.OnClickListener {

    private static final String TAG = FilterActivity.class.getSimpleName();
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    ImageView imagePreview, back;
    TextView btnNext;
    RelativeLayout backLayout;
    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;
    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;
    RecyclerView recyclerView;
    ThumbnailsAdapter mAdapter;
    List<ThumbnailItem> thumbnailItemList;
    Uri selectedUri;
    int position = -1;
    StorageUtils storageManager;
    boolean isPermissionDialogNeeded = true;
    private Context mContext = null;
    private int imagePreviewWidth, imagePreviewHeight;

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        // For window status bar should be in black in lollipop and also below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }
        mContext = this;
        selectedUri = getIntent().getData();

        storageManager = StorageUtils.getInstance(this);
        imagePreview = (ImageView) findViewById(R.id.image_preview);
        btnNext = (TextView) findViewById(R.id.btnNext);
        backLayout = (RelativeLayout) findViewById(R.id.backLayout);

        ViewGroup.LayoutParams layoutParams = imagePreview.getLayoutParams();
        imagePreviewWidth = imagePreview.getDrawable().getIntrinsicWidth();
        imagePreviewHeight = imagePreview.getDrawable().getIntrinsicHeight();

        loadUriImage();

        backLayout.setOnClickListener(this);
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(mContext, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(mContext, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    public void onFilterSelected(Filter filter) {
        // reset image controls
        // resetControls();

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

    }

    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */

    private int exifToDegrees(int exifOrientation) {
        int degree = 0;
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            degree = 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            degree = 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            degree = 270;
        }
        Log.d(TAG, "exifToDegrees: " + degree);
        return degree;
    }

    private int getExifRotation(androidx.exifinterface.media.ExifInterface exifInterface) {
        int rotation = 0;
        int orientation = exifInterface.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        Log.d(TAG, "getExifRotation: " + rotation);
        return rotation;
    }

    private void loadUriImage() {
        imagePreview.setVisibility(View.VISIBLE);
        originalImage = storageManager.getBitMapFromUri(mContext, selectedUri);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        Glide.with(mContext)
                .load(selectedUri)
                .into(imagePreview);
        imagePreview.post(new Runnable() {
            @Override
            public void run() {
                imagePreviewWidth = imagePreview.getWidth();
                imagePreviewHeight = imagePreview.getHeight();
            }
        });

        loadView();
    }

    private void loadView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        int displayWidth = AppUtils.getDisplayWidth(this);
        int displayHeight = AppUtils.getDisplayHeight(this);
        thumbnailItemList = new ArrayList<>();
        mAdapter = new ThumbnailsAdapter(this, thumbnailItemList, this, displayWidth, displayHeight);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);
        prepareThumbnail(null);
    }

    public synchronized void prepareThumbnail(final Bitmap bitmap) {
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage;
                if (bitmap == null) {
                    thumbImage = getAdapterBitmap();
                    thumbImage = getExactOrientationBitmapThumb(thumbImage);
                } else {
                    thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//                    thumbImage = Bitmap.createScaledBitmap(thumbImage, imagePreviewWidth, imagePreviewHeight, false);
                    thumbImage = bitmap.copy(Bitmap.Config.RGB_565, false);
                    //                    String imagePath = storageManager.saveBitmap(bitmap, storageManager.getTempImageCacheDir(), storageManager.getFileName("checkname"));
                }

                if (thumbImage == null)
                    return;

                ThumbnailsManager.clearThumbs();
                thumbnailItemList.clear();

                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImage;
                thumbnailItem.filterName = getString(R.string.normal);
                ThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(FilterActivity.this);

                filters.remove(3);
                filters.remove(5);
                filters.remove(13);

                for (Filter filter : filters) {
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }

                thumbnailItemList.addAll(ThumbnailsManager.processThumbs(FilterActivity.this));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(r).start();
    }

    public Bitmap getAdapterBitmap() {
        return storageManager.getBitMapFromUri(mContext, selectedUri);
    }

    public Bitmap getExactOrientationBitmapThumb(Bitmap mBitmap) {
        try {
            // InputStream input = getContentResolver().openInputStream(Uri.fromFile(new File(selectedPath)));

            ExifInterface exif = null;

            InputStream in = mContext.getContentResolver().openInputStream(selectedUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exif = new ExifInterface(in);
            }

//                exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + getPhotoOrientation(configurationProvider.getSensorPosition()))
//                exif.saveAttributes()

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
            }

//            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getHeight() + 260, mBitmap.getHeight(), matrix, true);
//            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getWidth(), matrix, true);
            mBitmap = mBitmap.copy(Bitmap.Config.RGB_565, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mBitmap;

    }

    /*
     * saves image to camera gallery
     * */

    private void saveImageToGallery() {
        String fileName = mContext.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".jpg";
        selectedUri = storageManager.saveToSDCard(finalImage, Constants.TAG_IMAGE, fileName);
        addPostActivity(selectedUri);
    }

    public void addPostActivity(Uri selectedUri) {
        Intent intent = new Intent();
        intent.setData(selectedUri);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }


    // opening image in default image viewer app
    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backLayout:
                finish();
                break;
            case R.id.btnNext:
                saveImageToGallery();
                break;

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * verifiy all the permissions passed to the array
     *
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                FilterActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    public void AlertDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FilterActivity.this);
        alertDialogBuilder.setTitle("Permissions Required")
                .setMessage("You have forcefully denied some of the required permissions " +
                        "for this action. Please open settings, go to permissions and allow them.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }
}
