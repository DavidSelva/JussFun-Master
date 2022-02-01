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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.jussfun.R;
import com.app.jussfun.external.EndlessRecyclerOnScrollListener;
import com.app.jussfun.external.ProgressWheel;
import com.app.jussfun.external.toro.core.PlayerSelector;
import com.app.jussfun.external.toro.core.widget.Container;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.PermissionsUtils;
import com.app.jussfun.model.Feeds;
import com.app.jussfun.model.FeedsModel;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class FeedsFragment extends Fragment {

    private static final String TAG = FeedsFragment.class.getSimpleName();
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private FeedsAdapter feedsAdapter;

    // For use other fragment
    private FeedsFragment feedsFragment;
    public static boolean isDeleteStory = false;
    //below list for delete unwanted files
    public static int exploreClickItem = 0;
    boolean isChekRefresh = false;
    public static boolean isEdit = false;
    //If user click cmtButton multipletime ,should not open multipletime activity
    public static boolean IsCmtButtonClicked = false;
    public static List<String> unfollowList = new ArrayList<>();
    final Handler handler = new Handler();  // post a delay due to the visibility change
    ConstraintLayout parentLay;
    ImageView btnPost;
    Container container;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    //    RecyclerView rvStories;
    LinearLayoutManager layoutManager;
    ProgressWheel pgsBar;
    LinearLayout newUserLayout;
    // used for pull down refresh home page
    SwipeRefreshLayout swipeRefreshLayout;
    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
    Call<FeedsModel> homeApiCall;
    ArrayList<Feeds> feedsList = new ArrayList<>();
    PlayerSelector selector = PlayerSelector.DEFAULT; // visible to user by default.
    BottomSheetDialog dialog;
    Dialog newDialog;
    private NestedScrollView nestedScrollView;
    private Context mContext;
    private String wayType = "";
    private int visibleItemCount;
    private int pastVisiblesItems;
    private boolean isLoading = true;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private int offset = 0, limitCnt = 10;
    private EndlessRecyclerOnScrollListener scrollListener;
    private boolean isLoadedAllItems = false;
    private int SelecthomeListposition;
    private ActivityResultLauncher<String[]> mPermissionResult;
    public static boolean homeMessageBadge = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle bundle) {
        feedsFragment = this;
        unfollowList = new ArrayList<>();
        return inflater.inflate(R.layout.home_post_list_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initPermission();
        findHeightWidth();
        setMargins();
        feedsList = new ArrayList<>();
        container.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        container.setLayoutManager(layoutManager);
        ViewCompat.setNestedScrollingEnabled(container, false);


        feedsAdapter = new FeedsAdapter(feedsList, getActivity(), getActivity());
        container.setAdapter(feedsAdapter);
//        container.setCacheManager(feedsAdapter);

        selector = PlayerSelector.DEFAULT;
        container.setPlayerSelector(selector);
        // get home post data from service
        if (NetworkReceiver.isConnected() && feedsList.size() == 0) {
//            loadHomeFeeds(offset, limitCnt);
        } else {
            pgsBar.setVisibility(View.GONE);
        }

        // for page scrolling
        ScrollMethod();

        // For pull to refresh listen
        pullToRefreshListener();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wayType = "post";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (PermissionsUtils.checkPermissionsArray(PermissionsUtils.HOMEPAGE_V11_PERMISSION, mContext)) {
                        goToAddPost();
                    } else {
                        mPermissionResult.launch(PermissionsUtils.HOMEPAGE_V11_PERMISSION);
                    }
                } else {
                    String[] permissions;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissions = PermissionsUtils.HOMEPAGE_V10_PERMISSION;
                    } else {
                        permissions = PermissionsUtils.HOMEPAGE_PERMISSION;
                    }
                    /*Dexter.withContext(mContext)
                            .withPermissions(permissions)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                    if (multiplePermissionsReport != null) {
                                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                            goToAddPost();
                                        } else if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                            new AlertDialog.Builder(getActivity())
                                                    .setMessage(getString(R.string.camera_storage_permission_description))
                                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                                    Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                                            startActivityForResult(intent, 600);
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                        }
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                    permissionToken.continuePermissionRequest();
                                }
                            }).onSameThread()
                            .check();*/
                }
            }
        });
    }

    private void initViews() {
//        nestedScrollView = getView().findViewById(R.id.nestedScrollView);
//        rvStories = getView().findViewById(R.id.story_view);
        container = getView().findViewById(R.id.homeRecycler);
        btnPost = getView().findViewById(R.id.btnPost);
        nullLay = getView().findViewById(R.id.nullLay);
        nullImage = getView().findViewById(R.id.nullImage);
        nullText = getView().findViewById(R.id.nullText);
        pgsBar = (ProgressWheel) getView().findViewById(R.id.pBar);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refreshlayout);
    }

    private void initPermission() {
        mPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }
                if (granted) {
                    if (wayType.equals("story")) {
                        goToAddStory();
                    } else {
                        goToAddPost();
                    }
                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {/*
                        if (!x.getValue()) {
                            if (shouldShowRequestPermissionRationale(x.getKey())) {
                                mPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, mContext.getString(R.string.camera_storage_permission_description));
                            }
                            break;
                        }
                    */
                    }
                }

            }
        });
    }

    private void setMargins() {
//        float horizontalSpacing = mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
        float horizontalSpacing = AppUtils.dpToPx(mContext, 10);
        float topSpace = AppUtils.dpToPx(mContext, 15);
//        float verticalSpacing = mContext.getResources().getDimension(R.dimen.post_bottom_margin);
        float verticalSpacing = AppUtils.dpToPx(mContext, 15);
/*        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(container.getContext(),
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
        container.addItemDecoration(horizontalDecoration);*/
        container.addItemDecoration(new RecyclerView.ItemDecoration() {
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

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void ScrollMethod() {

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) container.getLayoutManager();
        container.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        newUserLayout.setVisibility(View.GONE);
//        storyList.clear();
//        storyAdapter.notifyDataSetChanged();
        feedsList.clear();
        feedsAdapter.notifyDataSetChanged();
        // get home post data from service
        if (NetworkReceiver.isConnected()) {
//            loadHomeFeeds(offset, limitCnt);
        } else
            pgsBar.setVisibility(View.GONE);

    }

    public void loadHomeFeeds(int offsetCnt, int limitcnt) {
        homeApiCall = apiService.getHomeFeed(GetSet.getUserId(), "" + (offsetCnt * limitcnt), limitcnt + "");
        homeApiCall.enqueue(new Callback<FeedsModel>() {
            @Override
            public void onResponse(Call<FeedsModel> call, Response<FeedsModel> response) {
                if (offset > 0) feedsAdapter.removeLoadingView();
                pgsBar.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                if (offsetCnt == 0)
                    feedsList.clear();
                if (response.isSuccessful()) {
                    List<Feeds> results = response.body().getResult();
                    if (response.body().getStatus().equalsIgnoreCase("true")) {
                        if (results.size() > 0) {
                            isChekRefresh = true;
                            newUserLayout.setVisibility(View.GONE);
                            isLoading = false;
                            int prevSize = feedsList.size() + 1;
                            feedsList.addAll(results);
                            feedsAdapter.notifyItemRangeInserted(
                                    prevSize, feedsList.size()
                            );
//                                homelist.addAll(results);
//                                adapter.notifyDataSetChanged();
                        } else {
//                            isChekRefresh = false;
                            if (offsetCnt == 0)
                                newUserLayout.setVisibility(View.VISIBLE);
                            else
                                newUserLayout.setVisibility(View.GONE);
                            feedsList.addAll(results);
                            feedsAdapter.notifyDataSetChanged();
                            isLoading = true;
                        }
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Call<FeedsModel> call, Throwable t) {
                Log.e(TAG, "loadHomeFeeds: " + t.getMessage());
                pgsBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
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
            if (container != null) container.setPlayerSelector(selector);
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
            if (container != null) container.setPlayerSelector(selector);
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
        newUserLayout.setVisibility(View.GONE);
//        storyList.clear();
//        storyAdapter.notifyDataSetChanged();
        feedsList.clear();
        feedsAdapter.notifyDataSetChanged();
        // get home post data from service
        if (NetworkReceiver.isConnected()) {
//            loadHomeFeeds(offset, limitCnt);
        } else
            pgsBar.setVisibility(View.GONE);
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

}
