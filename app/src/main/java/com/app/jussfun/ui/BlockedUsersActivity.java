package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.app.jussfun.R;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.model.SearchUserResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockedUsersActivity extends BaseFragmentActivity {

    private static final String TAG = BlockedUsersActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.txtSubTitle)
    AppCompatTextView txtSubTitle;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    AppCompatTextView nullText;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;
    @BindView(R.id.shimmerLayout)
    ShimmerFrameLayout shimmerLayout;

    private AppUtils appUtils;
    private DBHelper dbHelper;
    private ApiInterface apiInterface;
    public List<ProfileResponse> usersList = new ArrayList<>();
    public BlockedAdapter blockedAdapter;
    private LinearLayoutManager mLayoutManager;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private boolean isLoading = true;
    int currentPage = 0, limit = 20;
    public static boolean hasBlockChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);
        ButterKnife.bind(this);
        appUtils = new AppUtils(this);
        dbHelper = DBHelper.getInstance(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
        loadAd();
    }

    private void initView() {
        btnBack.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }
        txtTitle.setVisibility(View.VISIBLE);
        txtSubTitle.setVisibility(View.VISIBLE);
        nullImage.setVisibility(View.GONE);
        nullText.setText(R.string.no_user_you_blocked_yet);
        txtTitle.setText(R.string.blocked_list);
        txtSubTitle.setText(R.string.unblock_anytime);
        txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        shimmerLayout.startShimmer();

        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        blockedAdapter = new BlockedAdapter(this, usersList);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(blockedAdapter);
        blockedAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 0;
                getBlockedList(currentPage);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        getBlockedList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
        getBlockedList(currentPage = 0);
    }

    private void getBlockedList(int offset) {
        if (NetworkReceiver.isConnected()) {
            nullLay.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                blockedAdapter.showLoading(true);
            }
            Call<SearchUserResponse> call = apiInterface.getBlockedList(GetSet.getUserId(), offset * 20, limit);
            call.enqueue(new Callback<SearchUserResponse>() {
                @Override
                public void onResponse(Call<SearchUserResponse> call, Response<SearchUserResponse> response) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        usersList.clear();
                    }
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            nullLay.setVisibility(View.GONE);
                            usersList.addAll(response.body().getUserList());
                        }
                    }
                    nullLay.setVisibility(usersList.size() == 0 ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(usersList.size() > 0 ? View.VISIBLE : View.GONE);

                    if (swipeRefreshLayout.isRefreshing()) {
                        blockedAdapter.showLoading(false);
                        swipeRefresh(false);
                        blockedAdapter.notifyDataSetChanged();
                        isLoading = true;
                    } else {
                        blockedAdapter.showLoading(false);
                        blockedAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<SearchUserResponse> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!swipeRefreshLayout.isRefreshing()) {
                blockedAdapter.showLoading(false);
            } else {
                if (usersList.size() == 0) {
                    nullText.setVisibility(View.VISIBLE);
                    nullText.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            MobileAds.initialize(this,
                    AdminData.googleAdsId);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasBlockChanges) {
            hasBlockChanges = false;
            swipeRefresh(true);
            getBlockedList(currentPage = 0);
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    public class BlockedAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;

        private List<ProfileResponse> itemList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public BlockedAdapter(Context context, List<ProfileResponse> followersList) {
            this.context = context;
            this.itemList = followersList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_blocked_user, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof MyViewHolder) {
                final ProfileResponse profile = itemList.get(position);

                if (profile.getPrivacyAge().equals(Constants.TAG_TRUE)) {
                    ((MyViewHolder) holder).txtName.setText(profile.getName());
                } else {
                    ((MyViewHolder) holder).txtName.setText(profile.getName() + ", " + profile.getAge());
                }

                if (profile.getGender().equals(Constants.TAG_MALE)) {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                } else {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                }

                ((MyViewHolder) holder).txtLocation.setText(profile.getLocation());

                ((MyViewHolder) holder).premiumImage.setVisibility(profile.getPremiumMember().equals(Constants.TAG_TRUE) ?
                        View.VISIBLE : View.GONE);
                Glide.with(context)
                        .load(Constants.IMAGE_URL + profile.getUserImage())
                        .apply(new RequestOptions().error(R.drawable.avatar).placeholder(R.drawable.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(((MyViewHolder) holder).userImage);

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = itemList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.premiumImage)
            ImageView premiumImage;
            @BindView(R.id.txtName)
            AppCompatTextView txtName;
            @BindView(R.id.genderImage)
            ImageView genderImage;
            @BindView(R.id.txtLocation)
            AppCompatTextView txtLocation;
            @BindView(R.id.btnUnBlock)
            ImageView btnUnBlock;
            @BindView(R.id.itemLay)
            ConstraintLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                DisplayMetrics displayMetrics = new DisplayMetrics();
            }

            @OnClick({R.id.itemLay, R.id.btnUnBlock})
            public void onViewClicked(View view) {
                if (view.getId() == R.id.itemLay) {
                    ProfileResponse profile = itemList.get(getAdapterPosition());
                    Intent intent = new Intent(context, OthersProfileActivity.class);
                    intent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                    intent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                    intent.putExtra(Constants.TAG_AGE, "" + profile.getAge());
                    intent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
                    intent.putExtra(Constants.TAG_GENDER, profile.getGender());
                    intent.putExtra(Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                    intent.putExtra(Constants.TAG_LOCATION, profile.getLocation());
                    intent.putExtra(Constants.TAG_PRIVACY_AGE, profile.getPrivacyAge());
                    intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, profile.getPrivacyContactMe());
                    intent.putExtra(Constants.TAG_PREMIUM_MEBER, profile.getPremiumMember());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_BLOCKED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                } else if (view.getId() == R.id.btnUnBlock) {
                    App.preventMultipleClick(btnUnBlock);
                    unlockBlockUser(itemList.get(getAdapterPosition()), getAdapterPosition());
                }
            }
        }
    }

    private void unlockBlockUser(ProfileResponse profile, final int selectedPosition) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, profile.getUserId());
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (responseMap.get(Constants.TAG_BLOCK_STATUS).equals("0")) {
                                usersList.remove(selectedPosition);
                                blockedAdapter.notifyItemRemoved(selectedPosition);
                                String chatId = GetSet.getUserId() + profile.getUserId();
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, profile.getUserId(), profile.getName(), profile.getUserImage(), Constants.TAG_FALSE);
                                }
                                nullLay.setVisibility(usersList.size() == 0 ? View.VISIBLE : View.GONE);
                                recyclerView.setVisibility(usersList.size() > 0 ? View.VISIBLE : View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progressBar)
        ProgressBar progressBar;

        public FooterViewHolder(View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }
    }
}
