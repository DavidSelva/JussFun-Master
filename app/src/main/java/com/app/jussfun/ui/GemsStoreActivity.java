package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.app.jussfun.R;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.OnOkCancelClickListener;
import com.app.jussfun.model.GemsPurchaseRequest;
import com.app.jussfun.model.GemsPurchaseResponse;
import com.app.jussfun.model.GemsStoreResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GemsStoreActivity extends BaseFragmentActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = GemsStoreActivity.class.getSimpleName();
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.6f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    private final int VIEW_TYPE_BANNER = 2;
    private final int VIEW_TYPE_PLACEHOLDER = 3;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtAvailableGems)
    TextView txtAvailableGems;
    @BindView(R.id.txtDescription)
    TextView txtDescription;
    @BindView(R.id.btnFreeGems)
    Button btnFreeGems;
    @BindView(R.id.headerLay)
    LinearLayout headerLay;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.txtNoGems)
    TextView txtNoGems;
    GemsAdapter gemsAdapter;
    List<GemsStoreResponse.GemsList> gemsList = new ArrayList<>();
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.txtGems)
    TextView txtGems;
    @BindView(R.id.parentLay)
    CoordinatorLayout parentLay;
    ApiInterface apiInterface;
    DialogCreditGems alertDialog;
    private int currentPage = 0, limit = 100;
    private GridLayoutManager mLayoutManager;
    private boolean isLoading = true, hasLoadMore = true, isSwipeEnabled = true;
    private BillingClient billingClient;
    private String purchaseSku = "", purchasePrice = "", purchaseCurrency = "";
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    private AppUtils appUtils;
    private PurchasesUpdatedListener purchasesUpdatedListener;

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_gems_store);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                //  if(getIntent().getStringExtra("OnCLick") != null && getIntent().getStringExtra("OnCLick").equals("ClickHere")){
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (int i = 0; i < purchases.size(); i++) {
                        if (purchaseSku.equals(purchases.get(i).getSkus().get(i))) {
                            payInApp(purchases.get(i));
                            //Consume Item to purchase again.
                            if (purchases.get(i).getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                ConsumeParams params = ConsumeParams.newBuilder()
                                        .setPurchaseToken(purchases.get(i).getPurchaseToken())
                                        .build();
                                billingClient.consumeAsync(params, new ConsumeResponseListener() {
                                    @Override
                                    public void onConsumeResponse(BillingResult billingResult, String s) {

                                    }
                                });
                            }
                            break;
                        }
                        //     }
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    App.makeToast(getString(R.string.purchase_cancelled));
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    // Handle an error caused by if item already owned.
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
                        @Override
                        public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> purchasesList) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                for (PurchaseHistoryRecord purchase : purchasesList) {
                                    if (purchaseSku.equals(purchase.getSkus().get(0))) {
                                        ConsumeParams params = ConsumeParams.newBuilder()
                                                .setPurchaseToken(purchase.getPurchaseToken())
                                                .build();
                                        billingClient.consumeAsync(params, new ConsumeResponseListener() {
                                            @Override
                                            public void onConsumeResponse(BillingResult billingResult, String s) {
                                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                                    initBilling(true);
                                                }
                                            }
                                        });
                                        break;
                                    }
                                }
                            }

                        }
                    });
                } else {
                    // Handle any other error codes.
//            App.makeToast("" + responseCode);
                }

            }
        };
        billingClient = BillingClient.newBuilder(GemsStoreActivity.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        appUtils = new AppUtils(this);


        // addon for interstitial advertisements
//        App.loadAds(this);
    }


    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        startAlphaAnimation(txtGems, 0, View.INVISIBLE);
        gemsList = getGemsPlaceHolder();
        gemsAdapter = new GemsAdapter(getApplicationContext(), gemsList);
        mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(gemsAdapter);
        gemsAdapter.notifyDataSetChanged();

        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (gemsAdapter.isPositionFooter(position)) {
                    return mLayoutManager.getSpanCount();
                } else if (gemsAdapter.getItemViewType(position) == VIEW_TYPE_BANNER) {
                    return mLayoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY > 0 && v.getChildAt(v.getChildCount() - 1) != null) {
                        if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                                scrollY > oldScrollY) {
                            //code to fetch more data for endless scrolling
                            if (isLoading && hasLoadMore) {
                                isLoading = false;
                                currentPage++;
                            }
                            if (hasLoadMore) {
                                // End has been reached
                                getFreeGems(currentPage);
                                isLoading = true;
                            }
                        }
                    }
                }
            });
        } else {
            nestedScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    View view = nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (nestedScrollView.getHeight() + nestedScrollView
                            .getScrollY()));

                    if (nestedScrollView.getScrollY() > 0 && diff == 0) {
                        if (isLoading && hasLoadMore) {
                            isLoading = false;
                            currentPage++;
                        }
                        if (hasLoadMore) {
                            // End has been reached
                            getFreeGems(currentPage);
                            isLoading = true;
                        }
                    }
                }
            });
        }

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

                handleAlphaOnTitle(percentage);
                handleToolbarTitleVisibility(percentage);
            }
        });


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(txtGems, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(txtGems, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(headerLay, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(headerLay, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    private List<GemsStoreResponse.GemsList> getGemsPlaceHolder() {
        List<GemsStoreResponse.GemsList> gemsLists = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            gemsLists.add(null);
        }
        return gemsLists;
    }

    private void getFreeGems(int offset) {
        if (NetworkReceiver.isConnected()) {
            txtNoGems.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing() && offset != 0) {
                gemsAdapter.showLoading(true);
            }

            Call<GemsStoreResponse> call = apiInterface.getGemsList(GetSet.getUserId(), Constants.TAG_ANDROID, offset * limit, limit);
            call.enqueue(new Callback<GemsStoreResponse>() {
                @Override
                public void onResponse(Call<GemsStoreResponse> call, Response<GemsStoreResponse> response) {
                    if (offset == 0) {
                        gemsList.clear();
                    }


                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        txtNoGems.setVisibility(View.GONE);

                        if (offset == 0 && response.body().getGemsList().size() < 10) {
                            gemsList.addAll(response.body().getGemsList());
                            initBilling(false);
                            /*Add banner*/
                            GemsStoreResponse.GemsList bannerItem = new GemsStoreResponse().new GemsList();
                            bannerItem.setViewType(VIEW_TYPE_BANNER);
                            gemsList.add(bannerItem);
                        } else {
                            int gemsListSize = response.body().getGemsList().size();
                            for (int i = 0; i < gemsListSize; i++) {
                                gemsList.add(response.body().getGemsList().get(i));
                                if ((i + 1) % 10 == 0) {
                                    /*Add banner*/
                                    GemsStoreResponse.GemsList bannerItem = new GemsStoreResponse().new GemsList();
                                    bannerItem.setViewType(VIEW_TYPE_BANNER);
                                    gemsList.add(bannerItem);
                                }
                            }
                            initBilling(false);
                        }
                    } else {
                        hasLoadMore = false;
                        if (gemsList.size() == 0) {
                            txtNoGems.setVisibility(View.GONE);
                            txtNoGems.setText(getString(R.string.no_gems_list_found));
                        }
                    }

                }

                @Override
                public void onFailure(Call<GemsStoreResponse> call, Throwable t) {
                    call.cancel();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!swipeRefreshLayout.isRefreshing()) {
                gemsAdapter.showLoading(false);
            } else {
                if (gemsList.size() == 0) {
                    txtNoGems.setVisibility(View.GONE);
                    txtNoGems.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
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

    @OnClick({R.id.btnBack, R.id.btnFreeGems})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnFreeGems:
                Intent freeGems = new Intent(getApplicationContext(), EarnGemsActivity.class);
                freeGems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(freeGems);
                break;
        }
    }

    @Override
    public void onRefresh() {
        currentPage = 0;
        hasLoadMore = true;
        getFreeGems(currentPage);
    }

    private void initBilling(boolean showAlert) {
        Log.d(TAG, "initBilling: " + showAlert);
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.d(TAG, "initBilling: " + billingResult.getResponseCode());
                Log.d(TAG, "initBilling: " + gemsList.size());
                int billingResponseCode = billingResult.getResponseCode();
                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "initBilling: " + "OK");
                    // The billing client is ready. You can query purchases here.
                    List<String> skuList = new ArrayList<>();
                    for (GemsStoreResponse.GemsList gems : gemsList) {
                        if (gems.getGemTitle() != null)
                            skuList.add(gems.getGemTitle());
                    }
                    Log.d(TAG, "initBilling: " + skuList.size());
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                                    Log.d(TAG, "initBilling: " + "Enter");
                                    // Process the result.
                                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                    if (showAlert) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            String price = skuDetails.getPrice();
                                            String currency = skuDetails.getPriceCurrencyCode();
                                            if (purchaseSku.equals(sku)) {
                                                Log.d(TAG, "initBilling: " + "Enter" + purchaseSku);
                                                purchasePrice = price;
                                                purchaseCurrency = currency;
                                                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                        .setSkuDetails(skuDetails)
                                                        .build();
                                                BillingResult result = billingClient.launchBillingFlow(GemsStoreActivity.this, flowParams);
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "initBilling: " + "Else");

                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            Log.d(TAG, "initBilling: " + "Else" + "for");
                                            purchaseCurrency = skuDetails.getPriceCurrencyCode();
                                            for (GemsStoreResponse.GemsList list : gemsList) {
                                                if (list.getViewType() == VIEW_TYPE_ITEM && list.getGemTitle().equals(skuDetails.getSku())) {
                                                    list.setGemPrice(skuDetails.getPrice());
                                                    Log.d(TAG, "initBilling: " + "Else" + "forif");
                                                    break;
                                                }
                                            }
                                        }
                                        if (swipeRefreshLayout.isRefreshing()) {
                                            Log.d(TAG, "initBilling: " + "Else" + "Refresh");
                                            gemsAdapter.showLoading(false);
                                            swipeRefresh(false);
                                            gemsAdapter.notifyDataSetChanged();
                                            isLoading = true;
                                        } else {
                                            Log.d(TAG, "initBilling: " + "Else" + "RefreshElse");
                                            Log.d(TAG, "initBilling: " + "Else" + gemsAdapter);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    gemsAdapter.showLoading(false);
                                                    Log.d(TAG, "initBilling: " + "Else" + gemsAdapter.showLoading);

                                                    gemsAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }


                                }

                            });
                }

            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "initBilling: " + "Disconnect");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void payInApp(Purchase purchase) {
        if (NetworkReceiver.isConnected()) {
            GemsPurchaseRequest request = new GemsPurchaseRequest();
            request.setUserId(GetSet.getUserId());
            request.setGemId(purchaseSku);
            request.setPaidAmount(purchaseCurrency + " " + purchasePrice);
            request.setTransactionId(purchase.getOrderId());
            Log.d(TAG, "payInApp: " + request.getGemId());
            Call<GemsPurchaseResponse> call = apiInterface.buyGems(request);
            call.enqueue(new Callback<GemsPurchaseResponse>() {
                @Override
                public void onResponse(Call<GemsPurchaseResponse> call, Response<GemsPurchaseResponse> response) {
                    Log.d(TAG, "payInApp: " + response.body().getAvailableGems());
                    GemsPurchaseResponse purchaseResponse = response.body();
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        showAlertDialog(getString(R.string.gems_purchase_response));

                        GetSet.setGems(Long.valueOf(purchaseResponse.getAvailableGems()));
                        SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                        txtAvailableGems.setText(GetSet.getGems() + " " + getString(R.string.gems));
                        Log.d(TAG, "payInApp: " + response.body().getAvailableGems());
                    }
                }

                @Override
                public void onFailure(Call<GemsPurchaseResponse> call, Throwable t) {
                    Log.d(TAG, "payInApp: " + t.toString());

                }
            });
        }
    }

    private void showAlertDialog(String message) {


        Log.d(TAG, "showAlertDialog: " + message);
        if (alertDialog != null && alertDialog.isAdded()) {
            return;
        }
        alertDialog = new DialogCreditGems();
        alertDialog.setContext(GemsStoreActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
            }

            @Override
            public void onCancelClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
            }
        });

        alertDialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFreeGems(currentPage);
        initView();
        txtAvailableGems.setText(GetSet.getGems() + " " + getString(R.string.gems));
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    public class GemsAdapter extends RecyclerView.Adapter {

        private final Context context;
        Typeface typefaceLight = null, typefaceRegular;
        private List<GemsStoreResponse.GemsList> gemsList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public GemsAdapter(Context context, List<GemsStoreResponse.GemsList> gemsList) {
            this.context = context;
            this.gemsList = gemsList;
            Log.d(TAG, "GemsAdapter: " + gemsList.size());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typefaceLight = getResources().getFont(R.font.font_light);
                typefaceRegular = getResources().getFont(R.font.font_regular);
            } else {
                typefaceLight = ResourcesCompat.getFont(context, R.font.font_light);
                typefaceRegular = ResourcesCompat.getFont(context, R.font.font_regular);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: " + viewType);
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_gems_store, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_PLACEHOLDER) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.placeholder_gems_store, parent, false);
                viewHolder = new PlaceHolder(itemView);
            } else if (viewType == VIEW_TYPE_BANNER) {
                View v;
                if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    v = LayoutInflater.from(context).inflate(R.layout.layout_prime_banner, parent, false);
                    viewHolder = new PrimeViewHolder(v);
                } else if (GetSet.isOncePurchased()) {
                    v = LayoutInflater.from(context).inflate(R.layout.layout_renewal_banner, parent, false);
                    viewHolder = new RenewalViewHolder(v);
                } else {
                    v = LayoutInflater.from(context).inflate(R.layout.layout_prime_banner, parent, false);
                    viewHolder = new PrimeViewHolder(v);
                }
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final GemsStoreResponse.GemsList gems = gemsList.get(position);
                ((MyViewHolder) holder).txtGemsCount.setText("" + Math.round(gems.getGemCount()));

                ((MyViewHolder) holder).txtGemsCount.setTextColor(getResources().getColor(R.color.colorBlack));

                ((MyViewHolder) holder).txtPrice.setText(gems.getGemPrice());

                ((MyViewHolder) holder).itemLay.setCardBackgroundColor(getResources().getColor(R.color.colorBg));
                ((MyViewHolder) holder).gemLay.setBackgroundColor(getResources().getColor(R.color.colorBg));

                Glide.with(context)
                        .load(Constants.GEMS_IMAGE_URL + gems.getGemIcon())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_diamond_yellow).error(R.drawable.ic_diamond_yellow).dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(((MyViewHolder) holder).giftImage);
            } else if (holder instanceof PlaceHolder) {
//                ((PlaceHolder) holder).shimmerLayout.setBackgroundColor(getResources().getColor(R.color.colorCardBg));
                ((PlaceHolder) holder).itemLay.setCardBackgroundColor(getResources().getColor(R.color.colorPlaceHoldersBg));
            } else if (holder instanceof PrimeViewHolder) {
                if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    ((PrimeViewHolder) holder).btnSubscribe.setVisibility(View.GONE);
                } else {
                    ((PrimeViewHolder) holder).btnSubscribe.setVisibility(View.VISIBLE);
                }
                ((PrimeViewHolder) holder).txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
                ((PrimeViewHolder) holder).txtPrice.setText(AppUtils.getPrimeContent(context));
            } else if (holder instanceof RenewalViewHolder) {
                RenewalViewHolder renewalViewHolder = (RenewalViewHolder) holder;
                ((RenewalViewHolder) holder).renewalLay.setVisibility(View.VISIBLE);

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
            else if (gemsList == null || gemsList.get(position) == null)
                return VIEW_TYPE_PLACEHOLDER;
            else if (gemsList.get(position).getViewType() == VIEW_TYPE_BANNER) {
                return VIEW_TYPE_BANNER;
            } else
                return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = gemsList.size();
            Log.d(TAG, "getItemCount: " + itemCount);

            if (showLoading)
                itemCount++;
            Log.d(TAG, "getItemCount: " + itemCount);
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
            notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.giftImage)
            ImageView giftImage;
            @BindView(R.id.txtGemsCount)
            TextView txtGemsCount;
            @BindView(R.id.txtPrice)
            TextView txtPrice;
            @BindView(R.id.itemLay)
            CardView itemLay;
            @BindView(R.id.gemLay)
            LinearLayout gemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                txtGemsCount.setTypeface(typefaceRegular);
                txtPrice.setTypeface(typefaceRegular);
            }

            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                App.preventMultipleClick(itemView);
                purchaseSku = gemsList.get(getAdapterPosition()).getGemTitle();
                initBilling(true);
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

        public class PrimeViewHolder extends RecyclerView.ViewHolder {

            /*Subscribe Lay*/
            @BindView(R.id.txtPrice)
            TextView txtPrice;
            @BindView(R.id.btnSubscribe)
            Button btnSubscribe;
            @BindView(R.id.subscribeLay)
            RelativeLayout subscribeLay;
            @BindView(R.id.txtPrimeTitle)
            TextView txtPrimeTitle;
            @BindView(R.id.primeBgLay)
            RelativeLayout primeBgLay;

            public PrimeViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 10, 5, 30);
                subscribeLay.setLayoutParams(params);
                txtPrimeTitle.setTypeface(typefaceLight);
                txtPrice.setTypeface(typefaceLight);
                btnSubscribe.setTypeface(typefaceLight);
                primeBgLay.setBackground(null);
            }

            @OnClick({R.id.btnSubscribe, R.id.subscribeLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.btnSubscribe:
                    case R.id.subscribeLay:
                        if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                            App.preventMultipleClick(btnSubscribe);
                            App.preventMultipleClick(subscribeLay);
                            Intent intent = new Intent(getApplicationContext(), PrimeActivity.class);
                            intent.putExtra("OnCLick", "ClickHere");
                            startActivityForResult(intent, 111);
                            //  startActivity(new Intent(getApplicationContext(), PrimeActivity.class));
                        }
                        break;
                }
            }
        }

        public class RenewalViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.btnRenewal)
            Button btnRenewal;
            @BindView(R.id.renewalBgLay)
            RelativeLayout renewalBgLay;
            @BindView(R.id.renewalLay)
            RelativeLayout renewalLay;
            @BindView(R.id.txtRenewalTitle)
            TextView txtRenewalTitle;
            @BindView(R.id.txtRenewalDes)
            TextView txtRenewalDes;

            public RenewalViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 10, 5, 30);
                renewalLay.setLayoutParams(params);
                txtRenewalTitle.setTypeface(typefaceLight);
                txtRenewalDes.setTypeface(typefaceLight);
                btnRenewal.setTypeface(typefaceLight);
                renewalBgLay.setBackground(null);
            }

            @OnClick({R.id.btnRenewal, R.id.renewalLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.renewalLay:
                    case R.id.btnRenewal:
                        Log.d(TAG, "onViewClicked: " + GetSet.getPremiumMember());
                        if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                            App.preventMultipleClick(btnRenewal);
                            App.preventMultipleClick(renewalLay);
                            Intent intent = new Intent(getApplicationContext(), PrimeActivity.class);
                            intent.putExtra("OnCLick", "ClickHere");
                            startActivityForResult(intent, 111);

                            // startActivity(new Intent(getApplicationContext(), PrimeActivity.class));
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111 && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: " + "Here");
            gemsAdapter.notifyDataSetChanged();
        }
    }
}
