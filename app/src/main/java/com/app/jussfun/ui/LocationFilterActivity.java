package com.app.jussfun.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.jussfun.R;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.AppDefaultResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrListener;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationFilterActivity extends BaseFragmentActivity implements SlidrListener {

    private static final String TAG = LocationFilterActivity.class.getSimpleName();
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
    private ApiInterface apiInterface;
    SwipeRefreshLayout swipeRefreshLayoutLocation;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    LocationAdapter locationAdapter;
    int resultCode = Activity.RESULT_CANCELED;
    boolean locationChanged = false, isAllLocationSelected = false;
    private List<String> tempLocationList = new ArrayList<>();
    private String from = null;

    public static List<String> homeLocationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_filter);
        ButterKnife.bind(this);
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        swipeRefreshLayoutLocation = findViewById(R.id.swipeRefreshLayoutLocation);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        Log.i(TAG, "LocationFilterActivityonCreat: "+from);
        Log.i(TAG, "LocationFilterActivityonCreate: "+tempLocationList);
        initView();

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .listener(this)
                .build();
        Slidr.attach(this, config);

        swipeRefreshLayoutLocation.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getAppDefaultData();

            }
        });
    }

    private void initView() {

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.location));

        if (from != null && from.equals("livza")) {
            tempLocationList = getIntent().getStringArrayListExtra(Constants.TAG_FILTER_LOCATION);
            isAllLocationSelected = getIntent().getBooleanExtra(Constants.TAG_LOCATION_SELECTED, false);
        } else if (from != null && from.equals(Constants.TAG_NEARBY)) {
            tempLocationList = getIntent().getStringArrayListExtra(Constants.TAG_FILTER_LOCATION);
            isAllLocationSelected = getIntent().getBooleanExtra(Constants.TAG_LOCATION_SELECTED, false);
        } else if (from != null && from.equals(Constants.TAG_PROFILE)) {
            if (getIntent().getStringExtra(Constants.TAG_LOCATION) != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.TAG_LOCATION))) {
                tempLocationList.clear();
                tempLocationList.add(getIntent().getStringExtra(Constants.TAG_LOCATION));
            }
        } else {
            if (GetSet.isLocationApplied()) {
                tempLocationList = AppUtils.filterLocation;
            } else {
                AppUtils.filterLocation = new ArrayList<>();
            }
        }

        if (from != null){
            if (from.equals("random")){
                tempLocationList = getIntent().getStringArrayListExtra(Constants.TAG_FILTER_LOCATION);
                isAllLocationSelected = getIntent().getBooleanExtra(Constants.TAG_LOCATION_SELECTED, false);
            }
        }
        if (from != null) {
            if (from.equals(Constants.TAG_PROFILE)) {
                List<String> location = AdminData.locationList;
                location.remove(getString(R.string.select_all));
                locationAdapter = new LocationAdapter(getApplicationContext(), location);
            } else if (from.equals(Constants.TAG_NEARBY)) {
                List<String> location = AdminData.locationList;
                locationAdapter = new LocationAdapter(getApplicationContext(), location);
            } else {
                locationAdapter = new LocationAdapter(getApplicationContext(), AdminData.locationList);
            }
        } else {
            locationAdapter = new LocationAdapter(getApplicationContext(), AdminData.locationList);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(locationAdapter);
        locationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    @Override
    public void onSlideStateChanged(int state) {

    }

    @Override
    public void onSlideChange(float percent) {

    }

    @Override
    public void onSlideOpened() {

    }

    @Override
    public boolean onSlideClosed() {
        setFilterResult();
        return false;
    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    if(swipeRefreshLayoutLocation.isRefreshing())
                        swipeRefreshLayoutLocation.setRefreshing(false);
                    AppDefaultResponse defaultData = response.body();
                    if (defaultData.getStatus().equals(Constants.TAG_TRUE)) {
                        AdminData.resetData();
                        AdminData.freeGems = defaultData.getFreeGems();
                        AdminData.giftList = defaultData.getGifts();
                        AdminData.giftsDetails = defaultData.getGiftsDetails();
                        AdminData.primeDetails = defaultData.getPrimeDetails();
                        AdminData.reportList = defaultData.getReports();
                        /*Add first item as Select all location filter*/
                        AdminData.locationList = new ArrayList<>();
                        AdminData.locationList.add(getString(R.string.select_all));
                        AdminData.locationList.addAll(defaultData.getLocations());
                        AdminData.membershipList = defaultData.getMembershipPackages();
                        AdminData.filterGems = defaultData.getFilterGems();
                        AdminData.filterOptions = defaultData.getFilterOptions();
                        AdminData.inviteCredits = defaultData.getInviteCredits();
                        AdminData.showAds = defaultData.getShowAds();
                        AdminData.showVideoAd = defaultData.getVideoAds();
                        AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                        AdminData.contactEmail = defaultData.getContactEmail();
                        AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                        AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                        AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                        AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                        AdminData.videoCallsGems = defaultData.getVideoCalls();
                        AdminData.gemConversion = defaultData.getGemConversion();
                        AdminData.giftConversion = defaultData.getGiftConversion();
                        initView();
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    call.cancel();
                    if(swipeRefreshLayoutLocation.isRefreshing())
                        swipeRefreshLayoutLocation.setRefreshing(false);
                }
            });
        }
    }

    public void setFilterResult() {
        Log.i(TAG, "setFilterResul: "+from);
        Log.i(TAG, "setFilterResult: "+tempLocationList.size());
        if (from != null && !from.equals(Constants.TAG_PROFILE)) {
            Intent data = new Intent();
            if (locationChanged) {
                resultCode = Activity.RESULT_OK;
                if (from != null && from.equals("livza")) {
                    if (tempLocationList.size() == 0) {
                        tempLocationList = AdminData.locationList;
                    }
                    data.putExtra(Constants.TAG_FILTER_LOCATION, (Serializable) tempLocationList);
                } else if (from != null && from.equals(Constants.TAG_NEARBY)) {
                    tempLocationList.remove(getString(R.string.select_all));
                    data.putExtra(Constants.TAG_FILTER_LOCATION, (Serializable) tempLocationList);
                }
                if (from != null && from.equals("random")){
                    if (tempLocationList.size() == 0) {
                        tempLocationList = AdminData.locationList;
                        data.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);
                    }
                    data.putExtra(Constants.TAG_FILTER_LOCATION, (Serializable) tempLocationList);
                    data.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);

                }
            }
            data.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);
            setResult(resultCode, data);
            finish();
        } else {
            finish();
        }
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    @Override
    public void onBackPressed() {
        setFilterResult();
    }

    public class LocationAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<String> locationList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public LocationAdapter(Context context, List<String> locationList) {
            this.context = context;
            this.locationList = locationList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_filter_location, parent, false);
            viewHolder = new MyViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final String filter = locationList.get(position);
            ((MyViewHolder) holder).txtLocation.setText(AppUtils.formatWord(filter));

            ((MyViewHolder) holder).viewLine.setBackgroundColor(getResources().getColor(R.color.colorDivider));

            ((MyViewHolder) holder).txtLocation.setTextColor(getResources().getColor(R.color.colorPrimaryText));

            Log.i(TAG, "LocationAdapteronBindViewHold: "+tempLocationList);
            if (tempLocationList.contains(filter)) {
                ((MyViewHolder) holder).btnSelect.setChecked(true);
                ((MyViewHolder) holder).checkLay.setBackground(context.getDrawable(R.drawable.circle_primary_bg));
            } else {
                ((MyViewHolder) holder).btnSelect.setChecked(false);
                ((MyViewHolder) holder).checkLay.setBackground(context.getDrawable(R.drawable.circle_white_outline));
            }
        }

        @Override
        public int getItemCount() {
            return locationList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txtLocation)
            TextView txtLocation;
            @BindView(R.id.btnSelect)
            CheckBox btnSelect;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;
            @BindView(R.id.checkLay)
            RelativeLayout checkLay;
            @BindView(R.id.viewLine)
            View viewLine;


            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @OnClick({R.id.btnSelect, R.id.itemLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.btnSelect:
                    case R.id.itemLay:
                    case R.id.checkLay:
                        if (from != null && from.equals(Constants.TAG_PROFILE)) {
                            btnSelect.setChecked(!btnSelect.isChecked());
                            tempLocationList.clear();
                            tempLocationList.add(locationList.get(getAdapterPosition()));
                            notifyDataSetChanged();
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_LOCATION, tempLocationList.get(0));
                            setResult(resultCode = Activity.RESULT_OK, intent);
                            finish();
                            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
                        } else {
                            btnSelect.setChecked(!btnSelect.isChecked());
                            updateLocations();
                        }
                        break;


                }
            }

            void updateLocations() {
                locationChanged = true;
                if (getAdapterPosition() == 0) {
                    if (btnSelect.isChecked()) {
                        isAllLocationSelected = true;
                        tempLocationList = new ArrayList<>();
                        AppUtils.filterLocation = new ArrayList<>();
                        tempLocationList.addAll(locationList);
                        AppUtils.filterLocation.add(Constants.TAG_GLOBAL);
                        AppUtils.filterLocation.addAll(locationList);
                        notifyDataSetChanged();
                    } else {
                        isAllLocationSelected = false;
                        tempLocationList = new ArrayList<>();
                        AppUtils.filterLocation = new ArrayList<>();
                        notifyDataSetChanged();
                    }
                } else {
                    if (btnSelect.isChecked()) {
                        tempLocationList.add(locationList.get(getAdapterPosition()));
                        if (tempLocationList.size() == (AdminData.locationList.size() - 1)) {
                            isAllLocationSelected = true;
                            tempLocationList = new ArrayList<>();
                            AppUtils.filterLocation = new ArrayList<>();
                            tempLocationList.addAll(locationList);
                            AppUtils.filterLocation.add(Constants.TAG_GLOBAL);
                            AppUtils.filterLocation.addAll(locationList);
                        } else {
                            isAllLocationSelected = false;
                            tempLocationList = AppUtils.filterLocation;
                        }
//                        Log.i(TAG, "updateLocations: " + new Gson().toJson(tempLocationList));
                        notifyDataSetChanged();
                    } else {
                        isAllLocationSelected = false;
                        tempLocationList.remove(getString(R.string.select_all));
                        tempLocationList.remove(locationList.get(getAdapterPosition()));
                        AppUtils.filterLocation = tempLocationList;
                        notifyDataSetChanged();
                    }
                }
            }
        }
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
}
