package com.app.jussfun.ui.feed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.base.BaseActivity;
import com.app.jussfun.external.SpacesItemDecoration;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FilterActivity extends BaseActivity implements ThumbnailsAdapter.ThumbnailsAdapterListener, View.OnClickListener {

    private static final String TAG = FilterActivity.class.getSimpleName();
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private TextView btnNext;
    private ImageView imagePreview;
    private RelativeLayout toolBarLay;
    private RelativeLayout backLayout;
    private MultiAutoCompleteTextView edtDescription;
    private RecyclerView recyclerView;
    private ConstraintLayout progressLay;
    private ProgressBar progressBar;
    private Switch btnComment;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;
    Bitmap finalImage;
    ThumbnailsAdapter mAdapter;
    List<ThumbnailItem> thumbnailItemList;
    Uri selectedUri;
    int position = -1;
    StorageUtils storageManager;
    boolean isPermissionDialogNeeded = true;
    private Context mContext = null;
    private int imagePreviewWidth, imagePreviewHeight;
    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

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
        initView();

        ViewGroup.LayoutParams layoutParams = imagePreview.getLayoutParams();
        imagePreviewWidth = imagePreview.getDrawable().getIntrinsicWidth();
        imagePreviewHeight = imagePreview.getDrawable().getIntrinsicHeight();

        loadUriImage();

        backLayout.setOnClickListener(this);
        btnNext.setOnClickListener(this);
    }

    private void initView() {
        btnNext = findViewById(R.id.btnNext);
        toolBarLay = findViewById(R.id.toolBarLay);
        backLayout = findViewById(R.id.backLayout);
        imagePreview = (ImageView) findViewById(R.id.image_preview);
        recyclerView = findViewById(R.id.recycler_view);
        edtDescription = findViewById(R.id.edtDescription);
        progressLay = findViewById(R.id.progressLay);
        progressBar = findViewById(R.id.progressBar);
        btnComment = findViewById(R.id.btnComment);
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
        prepareThumbnail();
    }

    public synchronized void prepareThumbnail() {
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage;
                thumbImage = getAdapterBitmap();

                if (thumbImage == null)
                    return;

                ThumbnailsManager.clearThumbs();
                thumbnailItemList.clear();

                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImage;
                thumbnailItem.filterName = getString(R.string.normal);
                /*thumbnailItemList.add(thumbnailItem);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyItemInserted(thumbnailItemList.size() -1);
                    }
                });*/
                ThumbnailsManager.addThumb(thumbnailItem);

                List<Filter> filters = FilterPack.getFilterPack(FilterActivity.this);

                filters.remove(3);
                filters.remove(5);
                filters.remove(13);

                for (Filter filter : filters) {
                    ThumbnailItem tI = new ThumbnailItem();
//                    tI.image = tI.filter.processFilter(thumbImage.copy(Bitmap.Config.ARGB_8888, true));
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                   /* thumbnailItemList.add(tI);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyItemInserted(thumbnailItemList.size() -1);
                        }
                    });*/
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(mContext.getContentResolver(),
                    selectedUri);
            try {
                return ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bitmap bitmap = storageManager.getBitMapFromUri(mContext, selectedUri);
        return bitmap;
    }

    /*
     * saves image to camera gallery
     * */

    private void saveImageToGallery() {
        String fileName = mContext.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".jpg";
        selectedUri = storageManager.saveToSDCard(finalImage, Constants.TAG_IMAGE, fileName);
        addFeed(selectedUri);
    }

    public void addFeed(Uri selectedUri) {
        if (NetworkReceiver.isConnected()) {
            uploadImage(selectedUri);
        } else {
            App.makeToast(mContext.getString(R.string.no_internet_connection));
        }
    }

    private void uploadImage(Uri fileUri) {
        try {
            showLoading();
            InputStream imageStream = mContext.getContentResolver().openInputStream(fileUri);
            RequestBody requestFile = RequestBody.create(storageManager.getBytes(imageStream), MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_FEED_IMAGE, "image.jpg", requestFile);
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
        requestMap.put(Constants.TAG_FEED_TYPE, Constants.TAG_IMAGE);
        requestMap.put(Constants.TAG_TITLE, "");
        requestMap.put(Constants.TAG_COMMENT_STATUS, btnComment.isChecked() ? "" + 1 : "" + 0);
        requestMap.put(Constants.TAG_DESCRIPTION, !TextUtils.isEmpty(edtDescription.getText()) ? edtDescription.getText().toString() : "");

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

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
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

    private void showLoading() {
        /*Disable touch options*/
        progressLay.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        progressLay.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
