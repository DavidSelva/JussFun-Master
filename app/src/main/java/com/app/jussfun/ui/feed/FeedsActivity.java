package com.app.jussfun.ui.feed;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.external.CustomTypefaceSpan;
import com.app.jussfun.external.EndlessRecyclerOnScrollListener;
import com.app.jussfun.external.toro.core.PlayerSelector;
import com.app.jussfun.external.toro.core.widget.Container;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.OnMenuClickListener;
import com.app.jussfun.helper.OnOkCancelClickListener;
import com.app.jussfun.helper.PermissionsUtils;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.model.FeedsModel;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.ui.BaseFragmentActivity;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedsActivity extends BaseFragmentActivity implements OnMenuClickListener {

    private static final String TAG = FeedsActivity.class.getSimpleName();

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.progressLay)
    ConstraintLayout progressLay;
    @BindView(R.id.containerFeed)
    Container containerFeed;
    @BindView(R.id.swipe_refreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    AppCompatTextView nullText;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;
    @BindView(R.id.btnPost)
    FloatingActionButton btnPost;
    @BindView(R.id.childLay)
    ConstraintLayout childLay;

    private Context mContext;
    private ApiInterface apiInterface;
    private AppUtils appUtils;
    private FeedsAdapter feedsAdapter;
    final Handler handler = new Handler();  // post a delay due to the visibility change
    Call<FeedsModel> homeApiCall;
    ArrayList<Feeds> feedsList = new ArrayList<>();
    PlayerSelector selector = PlayerSelector.DEFAULT; // visible to user by default.
    LinearLayoutManager layoutManager;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private boolean isLoading = true;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private int offset = 0, limitCnt = 10;
    private EndlessRecyclerOnScrollListener scrollListener;
    private boolean isLoadedAllItems = false;
    private StorageUtils storageUtils;

    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    private ActivityResultLauncher<String[]> mCameraPermissionResult;
    private ActivityResultLauncher<String[]> mStoragePermissionResult;
    private ActivityResultLauncher<Intent> cameraResultLauncher;
    private ActivityResultLauncher<Intent> galleryResultLauncher;
    private ActivityResultLauncher<Intent> addPostResultLauncher;
    private String followerId, feedId;
    private PopupMenu popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_feeds);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        mContext = this;
        followerId = getIntent().getStringExtra(Constants.TAG_USER_ID);
        if (getIntent().hasExtra(Constants.TAG_FEED_ID)) {
            feedId = getIntent().getStringExtra(Constants.TAG_FEED_ID);
        }
        initView();
        initPermission();
        initResultLauncher();
        setMargins();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        feedsList = new ArrayList<>();
        containerFeed.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        containerFeed.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(containerFeed, false);

        feedsAdapter = new FeedsAdapter(feedsList, this, this);
        containerFeed.setAdapter(feedsAdapter);
//        containerFeed.setCacheManager(feedsAdapter);

        selector = PlayerSelector.DEFAULT;
        containerFeed.setPlayerSelector(selector);
        // get home post data from service
        pullDownRefresh();

        // for page scrolling
        ScrollMethod();

        // For pull to refresh listen
        pullToRefreshListener();

        if (feedId != null) {
            btnPost.setVisibility(View.GONE);
        }
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageDialog();
            }
        });
    }

    private void initPermission() {
        mCameraPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }
                if (granted) {
                    captureImage();
                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(FeedsActivity.this, x.getKey())) {
                                mCameraPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OnOkCancelClickListener() {
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
                                }, getString(R.string.enable_camera_permission));
                            }
                            break;
                        }
                    }
                }

            }
        });

        mStoragePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }
                if (granted) {
                    openGallery();
                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(FeedsActivity.this, x.getKey())) {
                                mStoragePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OnOkCancelClickListener() {
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
                                }, mContext.getString(R.string.storage_permission_error));
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    private void initResultLauncher() {
        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
//                    Uri uri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(mContext, mCurrentPhotoUri), Constants.TAG_SENT, fileName);
                    openAddPostActivity(mCurrentPhotoUri);
                }
            }
        });

        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    openAddPostActivity(result.getData().getData());
                }
            }
        });

        addPostResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    /*Refresh Data*/
                    addPost(result.getData().getData());
                }
            }
        });
    }

    private void addPost(Uri data) {
        if (NetworkReceiver.isConnected()) {
            uploadImage(data);
        } else {
            App.makeToast(mContext.getString(R.string.no_internet_connection));
        }
    }

    private void openAddPostActivity(Uri data) {
        Intent intent = new Intent(mContext, FilterActivity.class);
        intent.setData(data);
        addPostResultLauncher.launch(intent);
    }

    private void openImageDialog() {
        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, findViewById(R.id.childLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(mContext, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(mContext, CAMERA) == PackageManager.PERMISSION_GRANTED) {
                pickerOptionsSheet.dismiss();
                captureImage();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mCameraPermissionResult.launch(PermissionsUtils.CAMERA_PERMISSION);
                } else {
                    mCameraPermissionResult.launch(PermissionsUtils.CAMERA_STORAGE_PERMISSION);
                }
            }
        });

        layoutGallery.setOnClickListener(v -> {
            if (PermissionsUtils.checkStoragePermission(mContext)) {
                pickerOptionsSheet.dismiss();
                openGallery();
            } else {
                mStoragePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
            }

        });

        pickerOptionsSheet.show();
    }

    private void captureImage() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (captureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //     Timber.e(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoUri = FileProvider.getUriForFile(mContext,
                        getString(R.string.file_provider_authority),
                        photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                cameraResultLauncher.launch(captureIntent);
            }
        }
    }

    private void openGallery() {
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
        pickIntent.setType("image/jpeg");

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");
        if (chooserIntent.resolveActivity(mContext.getPackageManager()) != null) {
            galleryResultLauncher.launch(chooserIntent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = storageUtils.getCacheDir(mContext);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoFile = image;
        return image;
    }

    private void setMargins() {
//        float horizontalSpacing = mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
        float horizontalSpacing = AppUtils.dpToPx(mContext, 10);
        float topSpace = AppUtils.dpToPx(mContext, 15);
//        float verticalSpacing = mContext.getResources().getDimension(R.dimen.post_bottom_margin);
        float verticalSpacing = AppUtils.dpToPx(mContext, 15);
/*        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(containerFeed.getContext(),
                DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                *//*int position = parent.getChildAdapterPosition(view);
                // hide the divider for the last child
                if (position == 1 || position == state.getItemCount() - 1) {
                    outRect.setEmpty();
                } else {
                    super.getItemOffsets(outRect, view, parent, state);
                }*//*
            }
        };
        Drawable horizontalDivider = ContextCompat.getDrawable(mContext, R.drawable.divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        containerFeed.addItemDecoration(horizontalDecoration);*/
        containerFeed.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildViewHolder(view).getAdapterPosition();
                int itemCount = state.getItemCount();
//                outRect.left = (int) horizontalSpacing;
//                outRect.right = (int) horizontalSpacing;
                /*if (position == 0) {
                    outRect.top = 0;
                } else {
                    outRect.top = 0;
                }*/
                outRect.top = (int) verticalSpacing;
                outRect.bottom = (int) verticalSpacing;
                /*if (position == itemCount - 1) {
                    outRect.bottom = 0;
                } else {
                    outRect.bottom = (int) verticalSpacing;
                }*/
            }
        });
    }

    private void uploadImage(Uri fileUri) {
        try {
            showLoading();
            InputStream imageStream = mContext.getContentResolver().openInputStream(fileUri);
            RequestBody requestFile = RequestBody.create(storageUtils.getBytes(imageStream), MediaType.parse("image/*"));
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
        requestMap.put(Constants.TAG_TITLE, "");
        requestMap.put(Constants.TAG_DESCRIPTION, "");

        Call<Map<String, String>> call = apiInterface.addFeed(requestMap);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                hideLoading();
                if (response.isSuccessful()) {
                    pullDownRefresh();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                hideLoading();
            }
        });
    }

    public void ScrollMethod() {

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) containerFeed.getLayoutManager();
        containerFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (dy > 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {

                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (NetworkReceiver.isConnected()) {
                            feedsAdapter.addLoadingView();
                            offset++;
                            loadHomeFeeds(offset, limitCnt);
                            isLoading = true;
                        }
                    }

                }
            }
        });

    }

    public void pullToRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullDownRefresh();
            }
        });

        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary)
        );

    }

    private void pullDownRefresh() {
        // initialize every time create fragment
        if (homeApiCall != null) homeApiCall.cancel();
        offset = 0;
        isLoading = true;
        isLoadedAllItems = false;
        nullLay.setVisibility(View.GONE);
        feedsList.clear();
        feedsAdapter.notifyDataSetChanged();
        // get home post data from service
        loadHomeFeeds(offset, limitCnt);

    }

    public void loadHomeFeeds(int offsetCnt, int limitcnt) {
        if (NetworkReceiver.isConnected()) {
//            showLoading();
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            if (feedId != null) {
                requestMap.put(Constants.TAG_FEED_ID, feedId);
                requestMap.put(Constants.TAG_FOLLOWER_ID, "");
            } else if (followerId != null) {
                requestMap.put(Constants.TAG_FOLLOWER_ID, followerId);
                requestMap.put(Constants.TAG_FEED_ID, "");
            } else {
                requestMap.put(Constants.TAG_FOLLOWER_ID, "");
                requestMap.put(Constants.TAG_FEED_ID, "");
            }
            requestMap.put(Constants.TAG_LIMIT, "" + limitcnt);
            requestMap.put(Constants.TAG_OFFSET, "" + (offsetCnt * limitcnt));

            homeApiCall = apiInterface.getHomeFeeds(requestMap);
            homeApiCall.enqueue(new Callback<FeedsModel>() {
                @Override
                public void onResponse(Call<FeedsModel> call, Response<FeedsModel> response) {
                    hideLoading();
                    if (offset > 0) feedsAdapter.removeLoadingView();
                    containerFeed.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (offsetCnt == 0)
                        feedsList.clear();
                    if (response.isSuccessful()) {
                        List<Feeds> results = response.body().getResult();
                        if (response.body().getStatus().equalsIgnoreCase("true")) {
                            if (results.size() > 0) {
                                isLoading = false;
                                int prevSize = feedsList.size() + 1;
                                feedsList.addAll(results);
                                feedsAdapter.notifyItemRangeInserted(
                                        prevSize, feedsList.size()
                                );
                            } else {
                                feedsList.addAll(results);
                                feedsAdapter.notifyDataSetChanged();
                                isLoading = true;
                            }
                        }
                        if (results.size() > 0) {
                            nullLay.setVisibility(View.GONE);
                        } else {
                            if (offsetCnt == 0) {
                                nullLay.setVisibility(View.VISIBLE);
                                nullText.setText(mContext.getString(R.string.no_feeds_description));
                            } else
                                nullLay.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<FeedsModel> call, Throwable t) {
                    Log.e(TAG, "loadHomeFeeds: " + t.getMessage());
                    hideLoading();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    public void manualRefresh() {
        if (homeApiCall != null) homeApiCall.cancel();
        offset = 0;
        isLoading = true;
        isLoadedAllItems = false;
        nullLay.setVisibility(View.GONE);
//        storyList.clear();
//        storyAdapter.notifyDataSetChanged();
        feedsList.clear();
        feedsAdapter.notifyDataSetChanged();
        // get home post data from service
        loadHomeFeeds(offset, limitCnt);

//        HomeListFragment.storyList.clear();
//        storyAdapter.notifyDataSetChanged();
//        homeAdapter.notifyItemRemoved(0);
//        getStories();
    }

    /*
        public void manualRefresh() {
            storyList.clear();
            storyAdapter.notifyDataSetChanged();
            getStories();
            loadHomeFeeds(offset, limitCnt);
        }
    */
    public void manualRefreshEdit() {
        feedsList.clear();
        feedsAdapter.notifyDataSetChanged();
//        loadHomeFeeds(offset, limitCnt);
    }

    private void showLoading() {
        /*Disable touch options*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressLay.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }

    public void hideLoading() {
        /*Enable touch options*/
        progressLay.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(mContext, findViewById(R.id.childLay), isConnected);
    }

    @Override
    public void onMenuClicked(View view, Feeds resultsItem, int adapterPosition) {
        openMenu(view, resultsItem, adapterPosition);
    }

    @Override
    public void onUserClicked(View view, Feeds resultsItem, int adapterPosition) {

    }

    private void openMenu(View view, Feeds resultsItem, int adapterPosition) {
        popupMenu = new PopupMenu(mContext, view, R.style.PopupMenuBackground);
        popupMenu.getMenuInflater().inflate(R.menu.feed_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.START);

        Typeface typeface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        if (!feedsList.get(adapterPosition).getUserId().equals(GetSet.getUserId())) {
            popupMenu.getMenu().getItem(0).setVisible(false);
        }
        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem menuItem = popupMenu.getMenu().getItem(i);
            SpannableString mNewTitle = new SpannableString(menuItem.getTitle());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mNewTitle.setSpan(new TypefaceSpan(typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                mNewTitle.setSpan(new CustomTypefaceSpan("", typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            menuItem.setTitle(mNewTitle);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(getString(R.string.delete_feed))) {
                    deleteFeed(resultsItem, adapterPosition);
                    popupMenu.dismiss();
                } else if (item.getTitle().toString().equals(getString(R.string.share))) {
                    shareFeed(resultsItem.getFeedId());
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void shareFeed(String feedId) {
        Task<ShortDynamicLink> shortLinkTask = appUtils.getFeedDynamicLink(feedId);

        shortLinkTask.addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String msg = String.format(getString(R.string.share_feed_description), getString(R.string.app_name), shortLink);
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
                } else {
                    // Error
                    // ...
                    Log.e(TAG, "onComplete: ");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    private void deleteFeed(Feeds item, int adapterPosition) {
        if (NetworkReceiver.isConnected()) {
            Call<Map<String, String>> call = apiInterface.deleteFeed(GetSet.getUserId(), item.getFeedId());
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> data = response.body();
                        if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            feedsList.remove(adapterPosition);
                            feedsAdapter.notifyItemRemoved(adapterPosition);
                            feedsAdapter.notifyItemRangeChanged(adapterPosition, feedsList.size());
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
        }
    }
}
