package com.app.jussfun.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.jussfun.R;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.AdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.google.android.gms.ads.AdView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FollowersActivity extends BaseFragmentActivity {

    private static final String TAG = FollowersActivity.class.getSimpleName();
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.shimmerLayout)
    ShimmerFrameLayout shimmerLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    String partnerId;
    FriendsFragment followingsFragment;
    WhosInterestFragment followersFragment;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    SlidrInterface slidrInterface;
    @BindView(R.id.adView)
    AdView adView;
    public static boolean hasInterestChange, hasFriendChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);
        partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        slidrInterface = Slidr.attach(this, config);
        shimmerLayout.startShimmer();
        initView();
    }

    private void initView() {
        setToolBarTitle(0);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected: " + position);
                setToolBarTitle(position);
                if (position == 0) {
                    enableSliding(true);
                } else {
                    enableSliding(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if ((getIntent().getStringExtra(Constants.TAG_ID) != null) &&
                (getIntent().getStringExtra(Constants.TAG_ID).equals(Constants.TAG_FOLLOWINGS))) {
            viewPager.setCurrentItem(0);
        } else {
            viewPager.setCurrentItem(1);
        }

        if (GetSet.getPremiumMember().equals(Constants.TAG_FALSE))
            loadAd();


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            AdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }


    private void setToolBarTitle(int position) {
        if (position == 0) {
            if (shimmerLayout.isShimmerStarted()) {
                shimmerLayout.stopShimmer();
            }
            shimmerLayout.startShimmer();

            txtTitle.setText(getString(R.string.followings));
            txtSubTitle.setText("");
            txtSubTitle.setText(R.string.swipe_to_see_friends);

            txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        } else {
            if (shimmerLayout.isShimmerStarted()) {
                shimmerLayout.stopShimmer();
            }
            shimmerLayout.startShimmer();

            txtTitle.setText(getString(R.string.friends));
            txtSubTitle.setText("");
            txtSubTitle.setText(R.string.swipe_to_see_whos_interested);

            txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 2;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                followingsFragment = new FriendsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                followingsFragment.setArguments(bundle);
                return followingsFragment;
            } else {
                followersFragment = new WhosInterestFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                followersFragment.setArguments(bundle);
                return followersFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
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

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void enableSliding(boolean enable) {
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }

}
