package com.app.jussfun.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.app.jussfun.R;
import com.app.jussfun.helper.AdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.OnOkCancelClickListener;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.app.jussfun.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.makeramen.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfileActivity extends BaseFragmentActivity {

    private static String TAG = MyProfileActivity.class.getSimpleName();
    @BindView(R.id.bannerImage)
    ImageView bannerImage;
    @BindView(R.id.profileImage)
    RoundedImageView profileImage;
    @BindView(R.id.btnEdit)
    ImageView btnEdit;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtLocation)
    TextView txtLocation;
    @BindView(R.id.txtFollowersCount)
    TextView txtFollowersCount;
    @BindView(R.id.txtFollowingsCount)
    TextView txtFollowingsCount;
    @BindView(R.id.txtGemsCount)
    TextView txtGemsCount;
    @BindView(R.id.txtGiftsCount)
    TextView txtGiftsCount;
    @BindView(R.id.btnRenewal)
    TextView btnRenewal;
    @BindView(R.id.renewalLay)
    RelativeLayout renewalLay;
    @BindView(R.id.genderImage)
    ImageView genderImage;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.followersLay)
    RelativeLayout followersLay;
    @BindView(R.id.followingsLay)
    RelativeLayout followingsLay;
    @BindView(R.id.premiumImage)
    ImageView premiumImage;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.txtPrimeTitle)
    TextView txtPrimeTitle;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.subscribeLay)
    RelativeLayout subscribeLay;
    @BindView(R.id.topLay)
    RelativeLayout topLay;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.imageLay)
    RelativeLayout imageLay;
    @BindView(R.id.iconDiamond)
    ImageView iconDiamond;
    @BindView(R.id.txtRenewalTitle)
    TextView txtRenewalTitle;
    @BindView(R.id.txtRenewalDes)
    TextView txtRenewalDes;
    @BindView(R.id.contentLay)
    LinearLayout contentLay;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.primeBgLay)
    RelativeLayout primeBgLay;
    @BindView(R.id.renewalBgLay)
    RelativeLayout renewalBgLay;

    ApiInterface apiInterface;
    @BindView(R.id.txtFollowers)
    TextView txtFollowers;
    @BindView(R.id.txtFollowings)
    TextView txtFollowings;
    private String partnerId = "", from;
    ProfileResponse othersProfile;
    private RequestOptions profileImageRequest;
    private AppUtils appUtils;
    private ProfileResponse profileResponse;
    private Context context;
    private DialogCreditGems alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.fragment_profile);
        ButterKnife.bind(this);
        context = this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .dontAnimate();
        initView();
        initProfile();
        getProfile();
        loadAd();
    }

    private void initView() {
        appUtils = new AppUtils(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AppUtils.getStatusBarHeight(getApplicationContext());
        toolbar.setLayoutParams(params);

        txtFollowers.setText(getString(R.string.friends));
        txtFollowings.setText(getString(R.string.whos_interested));


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            AdUtils.getInstance(this).loadAd(TAG, adView);}
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    private void initProfile() {

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
        btnBack.setImageDrawable(context.getDrawable(R.drawable.arrow_w_l));
        btnSettings.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_setting_strok));
        btnSettings.setColorFilter(ContextCompat.getColor(context, R.color.textPrimary));
        if (from != null && from.equals(Constants.TAG_QR_CODE)) {
            profileResponse = (ProfileResponse) getIntent().getSerializableExtra(Constants.TAG_PROFILE_DATA);
            setProfile(profileResponse);
        }
        if (GetSet.getUserId() != null) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            profileResponse.setLoginId(GetSet.getLoginId());
            profileResponse.setName(GetSet.getName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            profileResponse.setAge(GetSet.getAge());
            profileResponse.setFollowings(GetSet.getFollowingCount());
            profileResponse.setFollowers(GetSet.getFollowersCount());
            profileResponse.setLocation(GetSet.getLocation());
            profileResponse.setLoginId(GetSet.getLoginId());
            profileResponse.setAvailableGems("" + GetSet.getGems());
            profileResponse.setAvailableGifts("" + GetSet.getGifts());
            profileResponse.setVideosCount("" + GetSet.getVideos());
            profileResponse.setPremiumMember(GetSet.getPremiumMember());
            profileResponse.setPremiumExpiryDate(GetSet.getPremiumExpiry());
            profileResponse.setPrivacyAge(GetSet.getPrivacyAge());
            profileResponse.setPrivacyContactMe(GetSet.getPrivacyContactMe());
            setProfile(profileResponse);
        }
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null && NetworkReceiver.isConnected()) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        profileResponse = profile;
                        GetSet.setUserId(profile.getUserId());
                        GetSet.setLoginId(profile.getLoginId());
                        GetSet.setName(profile.getName());
                        GetSet.setDob(profile.getDob());
                        GetSet.setAge(profile.getAge());
                        GetSet.setGender(profile.getGender());
                        GetSet.setUserImage(profile.getUserImage());
                        GetSet.setLocation(profile.getLocation());
                        GetSet.setInterestsCount(profile.getInterests());
                        GetSet.setFriendsCount(profile.getFriends());
                        GetSet.setUnlocksLeft(profile.getUnlocksLeft());
                        GetSet.setFollowingCount(profile.getFollowings());
                        GetSet.setFollowersCount(profile.getFollowers());
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(profile.getAvailableGifts()) : 0L);
                        GetSet.setGems(profile.getAvailableGems() != null ? Long.parseLong(profile.getAvailableGems()) : 0L);
                        GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(profile.getVideosCount()) : 0L);
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        GetSet.setPrivacyAge(profile.getPrivacyAge());
                        GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());
                        GetSet.setShowNotification(profile.getShowNotification());
                        GetSet.setFollowNotification(profile.getFollowNotification());
                        GetSet.setChatNotification(profile.getChatNotification());
                        GetSet.setInterestNotification(profile.getInterestNotification());
                        GetSet.setGiftEarnings(profile.getGiftEarnings());
                        GetSet.setReferalLink(profile.getReferalLink());
                        GetSet.setCreatedAt(profile.getCreatedAt());

                        SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                        SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                        SharedPref.putString(SharedPref.NAME, GetSet.getName());
                        SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                        SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                        SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                        SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                        SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                        SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());
                        SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                        SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                        SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                        SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                        SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
                        SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                        SharedPref.putLong(SharedPref.GEMS, GetSet.getGems());
                        SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                        SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                        SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                        SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                        SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                        SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                        SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                        SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                        SharedPref.putBoolean(SharedPref.INTEREST_NOTIFICATION, GetSet.getInterestNotification());
                        SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                        SharedPref.putString(SharedPref.REFERAL_LINK, GetSet.getReferalLink());
                        SharedPref.putString(SharedPref.CREATED_AT, GetSet.getCreatedAt());
                        setProfile(profile);
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void setProfile(ProfileResponse profile) {
        setPrimeView(profile);
        txtName.setText(profile.getName() + ", " + profile.getAge());
        Glide.with(getApplicationContext())
                .load(Constants.IMAGE_URL + profile.getUserImage())
                .apply(App.getProfileImageRequest())
                .into(profileImage);

        genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                context.getResources().getDrawable(R.drawable.men) : context.getResources().getDrawable(R.drawable.women));
        txtLocation.setText(AppUtils.formatWord(profile.getLocation()));
        txtFollowersCount.setText(profile.getFollowers() != null ? profile.getFollowers() : "");
        txtFollowingsCount.setText(profile.getFollowings() != null ? profile.getFollowings() : "");
        txtGemsCount.setText((profile.getAvailableGems() != null ? profile.getAvailableGems() : "0"));
        txtGiftsCount.setText((profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0"));

        /*txtGiftsCount.setText(getString(R.string.gifts) + " " + (profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0"));
        txtGiftsCount.setText(getString(R.string.gifts) + " " + (profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0"));*/
    }

    private void setPrimeView(ProfileResponse profile) {
        if (profile.getPremiumMember().equals(Constants.TAG_TRUE)) {
            premiumImage.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            txtPrice.setText(AppUtils.getPrimeContent(context));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(View.GONE);
            premiumImage.setVisibility(View.GONE);
        } else {
            premiumImage.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            txtPrice.setText(AppUtils.getPrimeContent(context));
        }
    }

    @OnClick({R.id.profileImage, R.id.btnEdit, R.id.btnRenewal, R.id.renewalLay, R.id.btnSettings, R.id.followersLay,
            R.id.followingsLay, R.id.btnSubscribe, R.id.subscribeLay, R.id.gemsLay, R.id.giftsLay,
            R.id.btnBack})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profileImage:
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
                break;
            case R.id.btnEdit:
                App.preventMultipleClick(btnEdit);
                Intent profileIntent = new Intent(context, EditProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                break;
            case R.id.btnRenewal:
            case R.id.renewalLay:
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    Intent primeIntent = new Intent(context, PrimeActivity.class);
                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(primeIntent);
                }
                break;
            case R.id.btnSettings:
                Intent primeIntent = new Intent(context, SettingsActivity.class);
                primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(primeIntent);
                break;
            case R.id.followersLay:
                Intent followersIntent = new Intent(context, FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                followersIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followersIntent);
                break;
            case R.id.followingsLay:
                Intent followingsIntent = new Intent(context, FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                followingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followingsIntent);
                break;
            case R.id.giftsLay:
                if (GetSet.getGifts() == 0) {
                    if (alertDialog != null && alertDialog.isAdded()) {
                        return;
                    }
                    alertDialog = new DialogCreditGems();
                    alertDialog.setCallBack(new OnOkCancelClickListener() {
                        @Override
                        public void onOkClicked(Object o) {
                            alertDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onCancelClicked(Object o) {

                        }
                    });
                    alertDialog.setContext(context);
                    alertDialog.setMessage(getString(R.string.you_dont_have_any_gifts));
                    alertDialog.show(getSupportFragmentManager(), TAG);
                } else {
                    Intent giftIntent = new Intent(context, ConvertGiftActivity.class);
                    giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(giftIntent);
                }
                break;
            case R.id.gemsLay:

                Intent gemsIntent = new Intent(context, GemsStoreActivity.class);
                gemsIntent.putExtra("OnCLick", "ClickHere");
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
                break;
            case R.id.btnBack:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {
            if (context != null) {
                Glide.with(context)
                        .load(Constants.IMAGE_URL + GetSet.getUserImage())
                        .apply(App.getProfileImageRequest())
                        .into(profileImage);
            }
        } else if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_REQUEST_CODE) {
            if (context != null) {
                profileResponse = (ProfileResponse) data.getSerializableExtra(Constants.TAG_PROFILE_DATA);
                setProfile(profileResponse);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
        if (!this.isDestroyed()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }

    public void showLoading() {
        /*Disable touch options*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}


