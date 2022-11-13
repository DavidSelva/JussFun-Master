package com.app.jussfun.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.external.LoopViewPager;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.MembershipPackages;
import com.app.jussfun.model.PrimeBenefit;
import com.app.jussfun.model.SubscriptionRequest;
import com.app.jussfun.model.SubscriptionResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrimeActivity extends BaseFragmentActivity implements PurchasesUpdatedListener {

    private static final String TAG = PrimeActivity.class.getSimpleName();

    int displayHeight, displayWidth;
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
    @BindView(R.id.viewPager)
    LoopViewPager viewPager;
    @BindView(R.id.pagerIndicator)
    CircleIndicator pagerIndicator;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.adView)
    AdView adView;
    List<SkuDetails> skuDetailsList = new ArrayList<>();
    private PrimeAdapter primeAdapter;
    private SlidrInterface sliderInterface;
    // create new Person
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    private String purchaseSku = "", purchasePrice = "", purchaseCurrency = "", transactionId = "";
    // create new Person
    private BillingClient billingClient;
    ApiInterface apiInterface;
    int resultCode = Activity.RESULT_CANCELED;
    PackageAdapter packageAdapter;
    int selectedPosition = 0;
    List<String> skuList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        setContentView(R.layout.activity_prime);
        ButterKnife.bind(this);
        billingClient = BillingClient.newBuilder(PrimeActivity.this).enablePendingPurchases().setListener(this).build();
        initView();
        getPrimeDetails();
    }

    private void initView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        for (MembershipPackages membershipPackages : AdminData.membershipList) {
            skuList.add(membershipPackages.getSubsTitle());
        }
        initBilling(false);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.width = displayWidth;
        linearParams.height = (int) (displayHeight * 0.4);
        viewPager.setClipToPadding(false);
        viewPager.setPadding(40, 0, 40, 0);
        viewPager.setLayoutParams(linearParams);
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        sliderInterface = Slidr.attach(this, config);
        txtSubTitle.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setVisibility(View.VISIBLE);
        txtSubTitle.setVisibility(View.GONE);
        txtTitle.setText(getString(R.string.prime));

        /*Init Prime View*/
        /*txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
        txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        premiumLay.setVisibility(GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);
        btnSubscribe.setVisibility(!GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);*/

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > 0) {
                    sliderInterface.lock();
                } else {
                    sliderInterface.unlock();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        loadAd();


    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }


    private void getPrimeDetails() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        if (AdminData.primeDetails.getPrimeBenefits() != null && AdminData.primeDetails.getPrimeBenefits().size() > 0) {
            primeList.addAll(AdminData.primeDetails.getPrimeBenefits());
        }

        if (primeList.size() == 0) {
            pagerIndicator.setVisibility(View.GONE);
            primeList = getPlaceHolderList();
        } else {
            pagerIndicator.setVisibility(View.VISIBLE);
        }


        primeAdapter = new PrimeAdapter(getApplicationContext(), primeList);
        viewPager.setAdapter(primeAdapter);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                primeAdapter.notifyDataSetChanged();
            }
        });
        pagerIndicator.setViewPager(viewPager);
    }

    private List<PrimeBenefit> getPlaceHolderList() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        primeList.add(null);
        return primeList;
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }


    @OnClick({R.id.btnBack, R.id.btnSubscribe})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSubscribe:
                if (packageAdapter.getItemCount() > 0) {
                    purchaseSku = packageAdapter.getPackageList().get(selectedPosition).getProductId();
                }
                initBilling(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    private void initBilling(boolean openDialog) {
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(final BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    ArrayList<QueryProductDetailsParams.Product> tempList = new ArrayList<>();
                    for (String id : skuList) {
                        QueryProductDetailsParams.Product temp = QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(id)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build();
                        tempList.add(temp);
                    }
                    QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                            .setProductList(tempList)
                            .build();
                    billingClient.queryProductDetailsAsync(params,
                            new ProductDetailsResponseListener() {
                                @Override
                                public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
//                                    Log.d(TAG, "onSkuDetailsResponse: " + productDetailsList);

                                    // Process the result.
                                    if (openDialog) {
                                        for (ProductDetails productDetails : productDetailsList) {
                                            String sku = productDetails.getProductId();
                                            if (purchaseSku.equals(sku)) {
//                                                Log.d(TAG, "onSkuDetailsResponse: " + new Gson().toJson(skuDetails));
                                                if (productDetails.getSubscriptionOfferDetails() != null && !productDetails.getSubscriptionOfferDetails().isEmpty()) {
                                                    String price = productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                                    String currency = Currency.getInstance(productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode()).getCurrencyCode();

                                                    purchasePrice = price;
                                                    purchaseCurrency = currency;
                                                    BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                            .setProductDetails(productDetails)
                                                            .build();
                                                    ArrayList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<BillingFlowParams.ProductDetailsParams>() {
                                                        {
                                                            add(productDetailsParams);
                                                        }
                                                    };
                                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                            .setProductDetailsParamsList(productDetailsParamsList)
                                                            .build();
                                                    billingClient.launchBillingFlow(PrimeActivity.this, flowParams);
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "onSkuDetailsResponse Else: " + productDetailsList.toString());
                                        initPackageAdapter(productDetailsList);
                                    }
                                }

                            });
                }


            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void initPackageAdapter(List<ProductDetails> skuDetailsList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                packageAdapter = new PackageAdapter(getApplicationContext(), skuDetailsList);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(packageAdapter);
                packageAdapter.notifyDataSetChanged();
            }
        });

        if (skuDetailsList.size() > 0) {
            btnSubscribe.setVisibility(View.VISIBLE);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    sliderInterface.lock();
                } else sliderInterface.unlock();
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            if (getIntent().getStringExtra("OnCLick") != null && getIntent().getStringExtra("OnCLick").equals("ClickHere")) {
                for (Purchase purchase : purchases) {
                    if (purchaseSku.equals(purchase.getProducts().get(0))) {
                        acknowledgePurchase(purchase);
                        payInApp(purchase);
                        break;
                    }
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            App.makeToast(getString(R.string.purchase_cancelled));
        } else {
            // Handle any other error codes.
        }
    }

    private void acknowledgePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        Log.d(TAG, "onAcknowledgePurchaseResponse: " + new Gson().toJson(billingResult));
                    }
                });
            }
        }
    }

    private void payInApp(Purchase purchase) {
        if (NetworkReceiver.isConnected()) {
            SubscriptionRequest request = new SubscriptionRequest();
            request.setUserId(GetSet.getUserId());
            request.setMembershipId(purchaseSku);
            request.setPaidAmount(purchaseCurrency + " " + purchasePrice);
            request.setTransactionId(purchase.getOrderId());
            Log.d(TAG, "payInApp: " + request);
            Call<SubscriptionResponse> call = apiInterface.paySubscription(request);
            call.enqueue(new Callback<SubscriptionResponse>() {
                @Override
                public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> response) {
                    SubscriptionResponse subscription = response.body();
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        GetSet.setPremiumMember(subscription.getPremiumMember());
                        GetSet.setPremiumExpiry(subscription.getPremiumExpiryDate());
                        GetSet.setGems(subscription.getAvailableGems() != null ? Long.valueOf(subscription.getAvailableGems()) : 0);
                        GetSet.setOncePurchased(true);
                        SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                        SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                        SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                        SharedPref.putBoolean(SharedPref.ONCE_PAID, GetSet.isOncePurchased());
                        billingClient.endConnection();
                        resultCode = Activity.RESULT_OK;
                        setResult(resultCode);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    public class PrimeAdapter extends PagerAdapter {
        private final Context context;
        @BindView(R.id.sliderImage)
        ImageView sliderImage;
        @BindView(R.id.txtSliderTitle)
        TextView txtSliderTitle;
        @BindView(R.id.txtSliderDescription)
        TextView txtSliderDescription;
        @BindView(R.id.itemLay)
        LinearLayout itemLay;
        private List<PrimeBenefit> primeList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public PrimeAdapter(Context context, List<PrimeBenefit> primeList) {
            this.context = context;
            this.primeList = primeList;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView;
            if (primeList.get(position) == null) {
                itemView = LayoutInflater.from(context)
                        .inflate(R.layout.placeholder_item_prime, container, false);
            } else {
                final PrimeBenefit prime = primeList.get(position);
                itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_prime, container, false);
                ButterKnife.bind(this, itemView);
                itemLay.setGravity(Gravity.CENTER);

                txtSliderTitle.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                txtSliderDescription.setTextColor(getResources().getColor(R.color.colorPrimaryText));

                txtSliderTitle.setText(prime.getTitle());
                txtSliderDescription.setText(prime.getDescription());
                txtSliderDescription.setMovementMethod(new ScrollingMovementMethod());
                Glide.with(context)
                        .load(Constants.PRIME_IMAGE_URL + prime.getImage())
                        .apply(new RequestOptions().error(R.drawable.ic_gift_primary).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(sliderImage);
            }

            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return primeList.size();
        }

    }

    public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.MyViewHolder> {
        private final Context context;
        private List<ProductDetails> packageList = new ArrayList<>();
        private boolean showLoading = false;

        public PackageAdapter(Context context, List<ProductDetails> packageList) {
            this.context = context;
            this.packageList = packageList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_prime_pack, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int pos) {
            int position = holder.getAdapterPosition();
            ProductDetails pack = packageList.get(position);
//            Log.d(TAG, "onBindViewHolder: "+ new Gson().toJson(pack));
            if (position == selectedPosition) {
                holder.contentLay.setBackground(getDrawable(R.drawable.square_transparent_primary));
            } else {
                holder.contentLay.setBackground(getDrawable(R.drawable.rounded_transparent_white));
            }
            holder.txtPrimeTitle.setText(AppUtils.getPackageTitle(pack.getTitle(), context));
            if (pack.getSubscriptionOfferDetails() != null && pack.getSubscriptionOfferDetails().size() > 0) {
                holder.txtPrimePrice.setText(pack.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
            } else {
                holder.txtPrimePrice.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public List<ProductDetails> getPackageList() {
            return packageList;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtPrimeTitle)
            TextView txtPrimeTitle;
            @BindView(R.id.txtPrimePrice)
            TextView txtPrimePrice;
            @BindView(R.id.contentLay)
            LinearLayout contentLay;
            @BindView(R.id.itemLay)
            CardView itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemLay.getLayoutParams();
                params.width = (int) (displayWidth * 0.40);
//                params.height = displayHeight / 2;
                itemLay.setLayoutParams(params);
            }

            @OnClick({R.id.itemLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.itemLay:
                        selectedPosition = getAdapterPosition();
                        packageAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }
}



