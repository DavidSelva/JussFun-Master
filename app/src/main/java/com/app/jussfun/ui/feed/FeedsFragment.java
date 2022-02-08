/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
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

/**
 * A list of content that contains a {@link Container} as one of its child. We gonna use a
 * {@link PagerSnapHelper} to mimic a Pager-inside-RecyclerView. Other contents will be
 * normal text to preserve the performance and also to not make user confused.
 *
 * @author eneim (7/1/17).
 */


@SuppressWarnings("unused")
public class FeedsFragment extends Fragment implements OnMenuClickListener {

    private static final String TAG = FeedsFragment.class.getSimpleName();
    private Context mContext;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.progressLay)
    ConstraintLayout progressLay;
    @BindView(R.id.containerFeed)
    Container containerFeed;
    @BindView(R.id.swipe_refreshlayout)
    SwipeRefreshLayout swipeRefreshLayout;
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

    // For use other fragment
    private FeedsFragment feedsFragment;
    private FeedsAdapter feedsAdapter;
    boolean isChekRefresh = false;
    public static boolean isEdit = false;
    //If user click cmtButton multipletime ,should not open multipletime activity
    public static boolean IsCmtButtonClicked = false;
    final Handler handler = new Handler();  // post a delay due to the visibility change
    LinearLayoutManager layoutManager;


    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    Call<FeedsModel> homeApiCall;
    ArrayList<Feeds> feedsList = new ArrayList<>();
    PlayerSelector selector = PlayerSelector.DEFAULT; // visible to user by default.
    BottomSheetDialog dialog;
    private String wayType = "";
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
    private PopupMenu popupMenu;
    private AppUtils appUtils;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle bundle) {
        feedsFragment = this;
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);
        ButterKnife.bind(this, view);
       /* View loadingLay = view.findViewById(R.id.loadingLay);
        progressLay = loadingLay.findViewById(R.id.progressLay);
        progressBar = loadingLay.findViewById(R.id.progressBar);*/
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle bundle) {
        super.onViewCreated(itemView, bundle);
        if (mContext == null) mContext = getActivity();
        storageUtils = StorageUtils.getInstance(mContext);
        appUtils = new AppUtils(mContext);

        initPermission();
        initResultLauncher();
        findHeightWidth();
        setMargins();
        feedsList = new ArrayList<>();
        containerFeed.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        containerFeed.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(containerFeed, false);

        feedsAdapter = new FeedsAdapter(feedsList, getActivity(), this);
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

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageDialog(itemView);
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
                            if (shouldShowRequestPermissionRationale(x.getKey())) {
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
                            if (shouldShowRequestPermissionRationale(x.getKey())) {
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

    private void openImageDialog(View itemView) {
        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, itemView.findViewById(R.id.parentLay), false);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
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
            requestMap.put(Constants.TAG_FOLLOWER_ID, "");
            requestMap.put(Constants.TAG_FEED_ID, "");
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
                                isChekRefresh = true;
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
                            }
                            else
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

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        layoutManager = null;
        //adapter = null;
        selector = null;
        super.onDestroyView();
    }

    public void onPlayVideo(boolean isVisibletoUser) {

        if (isVisibletoUser) {
            selector = PlayerSelector.DEFAULT;
        } else {
            selector = PlayerSelector.NONE;
        }
        // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
        // which will cause our setup doesn't work well. We need a delay to make things work.
        handler.postDelayed(() -> {
            if (containerFeed != null) containerFeed.setPlayerSelector(selector);
        }, 500);

       /* if (feedsAdapter.challengecontainer != null) {
            handler.postDelayed(() -> {
                if (isVisibletoUser) feedsAdapter.challengecontainer.pause();
                else feedsAdapter.challengecontainer.play();
            }, 400);

        }*/
    }

    public void open(String type, Feeds homeParentpojo, int homeListposition, String comment_status, String postId, String follwerid) {/*
        // custom dialog
        dialog = new BottomSheetDialog(mContext, R.style.BottomSheetDialog);
        dialog.setContentView(R.layout.home_dialog);
        View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        BottomSheetBehavior.from(bottomSheet).setHideable(true);

        this.SelecthomeListposition = homeListposition;
        ArrayList<String> popupList = new ArrayList<>();

        if (type.equalsIgnoreCase("ownpost")) {
            popupList = utils.OwnPostList(comment_status);
        } else {
            popupList = utils.OtherPostList(homeParentpojo);
        }

        // set the custom dialog components - text, image and button
        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        PopupListAdapter replyAdapter = new PopupListAdapter(FeedsFragment.this, popupList, postId, homeListposition, follwerid, feedsList);
        recyclerView.setAdapter(replyAdapter);
        float horizontalSpacing = mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
        float verticalSpacing = App.dpToPx(mContext, 8);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildViewHolder(view).getAbsoluteAdapterPosition();
                int itemCount = state.getItemCount();
                outRect.left = (int) horizontalSpacing;
                outRect.right = (int) horizontalSpacing;
                outRect.top = (int) verticalSpacing;
                if (position == itemCount - 1) {
                    outRect.bottom = (int) verticalSpacing;
                } else {
                    outRect.bottom = 0;
                }
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    */
    }

    /*@Override
    public void Reportcallback(int homeListposition) {
        feedsList.remove(homeListposition);
        homeListposition++;
        feedsAdapter.notifyItemRemoved(homeListposition);
        feedsAdapter.notifyItemRangeChanged(homeListposition, feedsList.size());
    }

    // When unfollow service completed , this method is invoked
    @Override
    public void Followcallback(int position, String followerId, String result, String followersCnt, String mutualStatus, RecyclerView.ViewHolder viewHolder) {
        if (result.equalsIgnoreCase("Followed successfully")) {
            int pos = position + 1;
            feedsList.get(position).setFollowStatus("true");
            feedsAdapter.notifyItemChanged(pos);
        } else {
            feedsList.remove(position);
            feedsAdapter.notifyItemRemoved(position);
        }
    }


    // When TurnOffOnCommentCallback service completed , this method is invoked

    // When Deletepost service completed , this method is invoked
    @Override
    public void DeletePostcallback(int homeListposition) {
        feedsList.remove(homeListposition);
//        homeListposition++;
        feedsAdapter.notifyItemRemoved(homeListposition);
        feedsAdapter.notifyItemRangeChanged(homeListposition, feedsList.size());
    }

    // when add comments in coment page, count will update in home page via this interface

    @Override
    public void TurnOffOnCommentCallback(int homeListposition, String status) {
        feedsList.get(homeListposition).setCommentStatus(status);
//        homeListposition++;
        feedsAdapter.notifyItemChanged(homeListposition);

    }

    @Override
    public void commentCntUpdate(String cmtCount, int homeParentPosition) {
        feedsList.get(homeParentPosition).setCommentCount(cmtCount);
        int pos = homeParentPosition + 1;
        feedsAdapter.notifyItemChanged(pos);

    }*/

    public void closeDialog() {
        dialog.dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void makeToast(String msg) {
        //Toast.makeText(context,""+msg,Toast.LENGTH_SHORT).show();
    }

    public void newUserPopup() {/*
        // custom dialog
        newDialog = new Dialog(getActivity());
        newDialog.setContentView(R.layout.newuser_popup);

        Button ok = (Button) newDialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newDialog.dismiss();
            }
        });

        newDialog.show();
    */
    }

    // when launch another page if video isplaying , video should pause
   /* @Override
    public void visibility(String visibleUser) {
        Log.e("visible", visibleUser);

        if (visibleUser.equalsIgnoreCase("true")) {
            selector = PlayerSelector.DEFAULT;
        } else {
            selector = PlayerSelector.NONE;
        }

        // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
        // which will cause our setup doesn't work well. We need a delay to make things work.
        handler.postDelayed(() -> {
            if (containerFeed != null) containerFeed.setPlayerSelector(selector);
        }, 500);
    }*/

    private void findHeightWidth() {
//        parentLay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                height = parentLay.getMeasuredHeight();
//                width = parentLay.getMeasuredWidth();
//            }
//        });

    }

   /* @Override
    public void onItemSelected(Object obj, int position) {
        if (AppPreferences.getIntFromStore(Preference_Constants.PREF_KEY_USER_STORY_COUNT) < Constants.MAX_STORY_COUNT) {
            wayType = "story";
            String[] permissionsArray;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                permissionsArray = PermissionsUtils.HOMEPAGE_V11_PERMISSION;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsArray = PermissionsUtils.HOMEPAGE_V10_PERMISSION;
            } else {
                permissionsArray = PermissionsUtils.HOMEPAGE_PERMISSION;
            }
            if (PermissionsUtils.checkPermissionsArray(permissionsArray, mContext)) {
                goToAddStory();
            } else {
                mPermissionResult.launch(permissionsArray);
            }
        } else {
            App.showToast(mContext, mContext.getString(R.string.max_story_description));
        }
    }*/

    private void goToAddPost() {
//        Intent intent = new Intent(mContext, GalleryActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        startActivity(intent);
    }

    private void goToAddStory() {
//        Intent intent = new Intent(mContext, StoryCameraKitActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        mContext.startActivity(intent);
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

    public void navigateToShare(Bundle bundle) {/*
        Utils.openShareDialog(mContext, new OkayCancelCallback() {
            @Override
            public void onOkayClicked(Object o) {
                FragmentShare fragmentShare = FragmentShare.newInstance(bundle);
                fragmentShare.show(getChildFragmentManager(), TAG);
            }

            @Override
            public void onCancelClicked(Object o) {
                Task<ShortDynamicLink> shortLinkTask = Utils.getInstance().getPostShareLink(mContext, bundle.getString(Constants.TAG_PRODUCT_ID));
                shortLinkTask.addOnCompleteListener(feedsFragment.getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            String inviteApiLink = String.format(mContext.getString(R.string.post_share_description), mContext.getString(R.string.app_name)) + " " + shortLink;
//                            inviteSocketLink = GetSet.getName() + " " + getString(R.string.stream_share_description) + " " + shortLink;
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, inviteApiLink);
                            startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_link)));
                        } else {
                            // Error
                            App.showToast(mContext, mContext.getString(R.string.something_went_wrong));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        App.showToast(mContext, mContext.getString(R.string.something_went_wrong));
                    }
                });
            }
        });
    */
    }

    public void navigateToShareExternal(Bundle bundle) {/*
        Task<ShortDynamicLink> shortLinkTask = Utils.getInstance().getPostShareLink(mContext, bundle.getString(Constants.TAG_PRODUCT_ID));
        shortLinkTask.addOnCompleteListener(feedsFragment.getActivity(), new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();
                    String inviteApiLink = String.format(mContext.getString(R.string.post_share_description), mContext.getString(R.string.app_name)) + " " + shortLink;
//                            inviteSocketLink = GetSet.getName() + " " + getString(R.string.stream_share_description) + " " + shortLink;
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, inviteApiLink);
                    startActivity(Intent.createChooser(intent, mContext.getString(R.string.share_link)));
                } else {
                    // Error
                    App.showToast(mContext, mContext.getString(R.string.something_went_wrong));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                App.showToast(mContext, mContext.getString(R.string.something_went_wrong));
            }
        });
    */
    }

   /* public void navigateToLikeList(String postId) {
        if (mFragmentNavigation != null) {
            mFragmentNavigation.pushFragment(LikedUsersFragment.newInstance(postId, "post"));
        }
    }*/

    private void showLoading() {
        /*Disable touch options*/
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressLay.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }

    public void hideLoading() {
        /*Enable touch options*/
        progressLay.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
            typeface = ResourcesCompat.getFont(mContext, R.font.font_light);
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

        shortLinkTask.addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
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
