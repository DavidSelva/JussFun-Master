package com.app.jussfun.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.app.jussfun.R;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.RecentHistoryResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
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

public class RecentHistoryActivity extends BaseFragmentActivity {

    private static final String TAG = RecentHistoryActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.txtPrimeTitle)
    TextView txtPrimeTitle;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.subscribeLay)
    RelativeLayout subscribeLay;
    ApiInterface apiInterface;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.txtNoFollowers)
    TextView txtNoFollowers;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    @BindView(R.id.btnRenewal)
    Button btnRenewal;
    @BindView(R.id.renewalLay)
    RelativeLayout renewalLay;
    @BindView(R.id.primeBgLay)
    RelativeLayout primeBgLay;
    @BindView(R.id.renewalBgLay)
    RelativeLayout renewalBgLay;
    private LinearLayoutManager mLayoutManager;
    private boolean isLoading = true, hasLoadMore = true;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    int currentPage = 0, limit = 10;
    private List<RecentHistoryResponse.UsersList> recentList = new ArrayList<>();
    RecentHistoryAdapter historyAdapter;
    private int displayHeight;
    private int displayWidth;
    SlidrInterface slidrInterface;
    AppUtils appUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_history);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        slidrInterface = Slidr.attach(this, config);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        appUtils = new AppUtils(this);
        txtSubTitle.setVisibility(View.GONE);
        txtTitle.setText(R.string.history);
        btnBack.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        btnSettings.setImageDrawable(getDrawable(R.drawable.delete));
        int padding = AppUtils.dpToPx(this, 12);
        btnSettings
                .setPadding(padding, padding, padding, padding);

        recentList.add(null);
        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        historyAdapter = new RecentHistoryAdapter(getApplicationContext(), recentList);
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(mLayoutManager);
        historyAdapter.notifyDataSetChanged();

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
                if (dx > 0) {
                    slidrInterface.lock();
                    if (mLayoutManager.findLastCompletelyVisibleItemPosition() == recentList.size() - 1) {
                        if (hasLoadMore) {
                            currentPage++;
                            getRecentMembers(currentPage);
                        }
                    }

                } else {
                    slidrInterface.unlock();
                }
            }
        });

        getRecentMembers(currentPage = 0);
        loadAd();
    }

    private List<RecentHistoryResponse.UsersList> getRecentPlaceHolder() {
        List<RecentHistoryResponse.UsersList> recentList = new ArrayList<>();
        recentList.add(null);
        return recentList;
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            MobileAds.initialize(RecentHistoryActivity.this,
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
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        initPrimeView();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    private void initPrimeView() {
        /*Init Prime View*/
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            renewalLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(View.GONE);
        } else {
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        }
    }

    private void getRecentMembers(int offset) {
        if (NetworkReceiver.isConnected()) {
            if (offset > 0) {
                historyAdapter.showLoading(true);
                historyAdapter.notifyItemChanged(recentList.size() - 1);
            }
            Call<RecentHistoryResponse> call = apiInterface.getRecentHistory(GetSet.getUserId(), offset * 10, limit);
            call.enqueue(new Callback<RecentHistoryResponse>() {
                @Override
                public void onResponse(Call<RecentHistoryResponse> call, Response<RecentHistoryResponse> response) {

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        if (offset == 0) recentList.clear();
                        txtNoFollowers.setVisibility(View.GONE);
                        recentList.addAll(response.body().getUsersList());
                        historyAdapter.showLoading(false);
                        historyAdapter.notifyDataSetChanged();
                    } else {
                        hasLoadMore = false;
                        historyAdapter.showLoading(false);
                        historyAdapter.notifyDataSetChanged();
                    }

                    if (recentList.size() == 0 || recentList.get(0) == null) {
                        btnSettings.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                        txtNoFollowers.setVisibility(View.VISIBLE);
                        txtNoFollowers.setText(getString(R.string.no_users_visited_yet));
                    } else {
                        btnSettings.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<RecentHistoryResponse> call, Throwable t) {
                    call.cancel();
                    historyAdapter.showLoading(false);
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            historyAdapter.showLoading(false);
            if (recentList.size() == 0) {
                txtNoFollowers.setVisibility(View.VISIBLE);
                txtNoFollowers.setText(getString(R.string.no_internet_connection));
            }
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    @OnClick({R.id.btnBack, R.id.btnSubscribe, R.id.subscribeLay, R.id.btnRenewal, R.id.renewalLay, R.id.btnSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
            case R.id.btnRenewal:
            case R.id.renewalLay:
                App.preventMultipleClick(btnSubscribe);
                App.preventMultipleClick(subscribeLay);
                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    Intent subscribe = new Intent(getApplicationContext(), PrimeActivity.class);
                    subscribe.putExtra("OnCLick", "ClickHere");
                    startActivity(subscribe);
                }
                break;
            case R.id.btnSettings:
                openClearHistoryDialog();
                break;
        }
    }


    private void openClearHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecentHistoryActivity.this, R.style.ThemeAlertDialog);
        builder.setMessage(getString(R.string.really_want_clear_history));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                clearRecentHistory();
                recentList.clear();
                historyAdapter.notifyDataSetChanged();
                btnSettings.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
                txtNoFollowers.setVisibility(View.VISIBLE);
                txtNoFollowers.setText(getString(R.string.no_users_visited_yet));
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    private void clearRecentHistory() {
        Call<HashMap<String, String>> call = apiInterface.clearRecentHistory(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                App.makeToast(getString(R.string.cleared_successfully));
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    public class RecentHistoryAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final int VIEW_TYPE_PLACEHOLDER = 2;
        private final Context context;


        private List<RecentHistoryResponse.UsersList> recentList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public RecentHistoryAdapter(Context context, List<RecentHistoryResponse.UsersList> recentList) {
            this.context = context;
            this.recentList = recentList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_recent_history, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_PLACEHOLDER) {
                View v = LayoutInflater.from(context).inflate(R.layout.placeholder_recent_history, parent, false);
                viewHolder = new PlaceHolder(v);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_horizontal_load_more, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final RecentHistoryResponse.UsersList user = recentList.get(position);

                if (user.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    ((MyViewHolder) holder).premiumImage.setVisibility(View.VISIBLE);
                } else {
                    ((MyViewHolder) holder).premiumImage.setVisibility(View.GONE);
                }
                ((MyViewHolder) holder).txtName.setText("" + user.getName());
                ((MyViewHolder) holder).txtAge.setVisibility(user.getPrivacyAge().equalsIgnoreCase(Constants.TAG_TRUE) ? View.GONE : View.VISIBLE);
                ((MyViewHolder) holder).txtAge.setText(", " + user.getAge());

                ((MyViewHolder) holder).btnFollow.setText(getString(R.string.more_info));
                ((MyViewHolder) holder).txtRecentTime.setText(AppUtils.getRecentDate(context, AppUtils.getTimeFromUTC(context, user.getLastChat())));
                Glide.with(context)
                        .load(Constants.IMAGE_URL + user.getUserImage())
                        .apply(App.getProfileImageRequest())
                        .into(((MyViewHolder) holder).profileImage);
                if (user.getGender().equals(Constants.TAG_MALE)) {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                } else {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                }
                ((MyViewHolder) holder).txtLocation.setText(AppUtils.formatWord(user.getLocation()));

            } else if (holder instanceof PlaceHolder) {

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
            else if (recentList.get(position) == null)
                return VIEW_TYPE_PLACEHOLDER;
            else
                return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = recentList.size();
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
            @BindView(R.id.txtRecentTime)
            TextView txtRecentTime;
            @BindView(R.id.txtName)
            TextView txtName;
            @BindView(R.id.txtAge)
            TextView txtAge;
            @BindView(R.id.genderImage)
            ImageView genderImage;
            @BindView(R.id.txtLocation)
            TextView txtLocation;
            @BindView(R.id.btnFollow)
            Button btnFollow;
            @BindView(R.id.profileLayout)
            LinearLayout profileLayout;
            @BindView(R.id.profileImage)
            RoundedImageView profileImage;
            @BindView(R.id.premiumImage)
            ImageView premiumImage;
            @BindView(R.id.itemLay)
            CardView itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                /*RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemLay.getLayoutParams();
                params.width = (int) (displayWidth * 0.70);
                txtName.setMaxWidth((int) (displayWidth * 0.30));
//                params.height = displayHeight / 2;
                itemLay.setLayoutParams(params);*/
            }

            @OnClick({R.id.itemLay, R.id.btnFollow})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.itemLay:
                    case R.id.btnFollow:
                        if (!recentList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                            Intent intent = new Intent(getApplicationContext(), OthersProfileActivity.class);
                            RecentHistoryResponse.UsersList item = recentList.get(getAdapterPosition());
                            intent.putExtra(Constants.TAG_PARTNER_ID, item.getUserId());
                            intent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());
                            intent.putExtra(Constants.TAG_AGE, "" + item.getAge());
                            intent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getUserImage());
                            intent.putExtra(Constants.TAG_GENDER, item.getGender());
                            intent.putExtra(Constants.TAG_BLOCKED_BY_ME, "");
                            intent.putExtra(Constants.TAG_LOCATION, item.getLocation());
                            intent.putExtra(Constants.TAG_FOLLOW, "");
                            intent.putExtra(Constants.TAG_PRIVACY_AGE, item.getPrivacyAge());
                            intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, item.getPrivacyContactMe());
                            intent.putExtra(Constants.TAG_FOLLOWERS, "-");
                            intent.putExtra(Constants.TAG_FOLLOWINGS, "-");
                            intent.putExtra(Constants.TAG_PREMIUM_MEBER, item.getPremiumMember());
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_RECENT);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                        break;
                }
            }
        }

        public class PlaceHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.shimmerLayout)
            ShimmerFrameLayout shimmerLayout;
            @BindView(R.id.itemLay)
            CardView itemLay;

            public PlaceHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                /*RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemLay.getLayoutParams();
                params.width = (int) (displayWidth * 0.70);
//                params.height = displayHeight / 2;
                itemLay.setLayoutParams(params);*/
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }
}
