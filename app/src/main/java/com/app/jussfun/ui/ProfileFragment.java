package com.app.jussfun.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.AdUtils;
import com.app.jussfun.helper.LocaleManager;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.makeramen.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static String TAG = ProfileFragment.class.getSimpleName();
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
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
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
    @BindView(R.id.txtFollowers)
    TextView txtFollowers;
    @BindView(R.id.txtFollowings)
    TextView txtFollowings;
    private Context context;
    ApiInterface apiInterface;
    ProfileResponse profileResponse;
    DialogCreditGems alertDialog;
    private String userId = null;
    private AppUtils appUtils;

    private InterstitialAd mInterstitialAd;
    private String from;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_USER_ID)) {
            userId = getArguments().getString(Constants.TAG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, rootView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(context);
        txtTitle.setText(getString(R.string.profile));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.bottomMargin = AppUtils.getNavigationBarHeight(getActivity()) + 10;
        scrollView.setLayoutParams(params);
        setHasOptionsMenu(true);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

//        loadAds();

        return rootView;
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            AdUtils.getInstance(context).loadAd(TAG, adView);
        }
    }

    private void loadAds() {
        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId("");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "onAdLoaded: ");
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("TAG", "FailedToLoad: " + errorCode);

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
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
                // Code to be executed when the interstitial ad is closed.
                if (from.equals("Gems")) {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    Intent freeGems = new Intent(getContext(), GemsStoreActivity.class);
                    freeGems.putExtra("OnCLick", "ClickHere");
                    freeGems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(freeGems);
                } else {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    Intent freeGems = new Intent(getContext(), SettingsActivity.class);
                    freeGems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(freeGems);
                }


            }
        });


    }

    private void initProfile() {

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setVisibility(View.INVISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
        btnSettings.setImageDrawable(context.getDrawable(R.drawable.icon_setting_strok));

        if (GetSet.getUserId() != null) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            profileResponse.setLoginId(GetSet.getLoginId());
            profileResponse.setName(GetSet.getName());
            profileResponse.setUserName(GetSet.getUserName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            profileResponse.setAge(GetSet.getAge());
            profileResponse.setInterests(GetSet.getInterestsCount());
            profileResponse.setFriends(GetSet.getFriendsCount());
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
        if (GetSet.getUserId() != null && apiInterface != null) {
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
                        GetSet.setUserName(profile.getUserName());
                        GetSet.setDob(profile.getDob());
                        GetSet.setAge(profile.getAge());
                        GetSet.setGender(profile.getGender());
                        GetSet.setUserImage(profile.getUserImage());
                        GetSet.setLocation(profile.getLocation());
                        GetSet.setFollowingCount(profile.getFollowings());
                        GetSet.setFollowersCount(profile.getFollowers());
                        GetSet.setInterestsCount(profile.getInterests());
                        GetSet.setFriendsCount(profile.getFriends());
                        GetSet.setUnlocksLeft(profile.getUnlocksLeft());
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

                        GetSet.setGiftCoversionEarnings(profile.getGiftCoversionEarnings());
                        GetSet.setGiftConversionValue(profile.getGiftConversionValue());

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

                        SharedPref.putString(SharedPref.GIFT_CONVERSION_EARNINGS, GetSet.getGiftCoversionEarnings());
                        SharedPref.putString(SharedPref.GIFT_CONVERSION_VALUE, GetSet.getGiftConversionValue());


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
        if (getActivity() != null && context != null) {
            setPrimeView(profile);
            txtName.setText(profile.getName() + ", " + profile.getAge());
            Glide.with(context)
                    .load(Constants.IMAGE_URL + profile.getUserImage())
                    .apply(App.getProfileImageRequest())
                    .into(profileImage);

            txtFollowers.setText(getString(R.string.whos_interested));
            txtFollowings.setText(getString(R.string.friends));
            txtFollowersCount.setText("" + profile.getInterests());
            txtFollowingsCount.setText("" + profile.getFriends());

            genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                    context.getResources().getDrawable(R.drawable.men) : context.getResources().getDrawable(R.drawable.women));
            txtLocation.setText(AppUtils.formatWord(profile.getLocation()));

            txtGemsCount.setText((profile.getAvailableGems() != null ? profile.getAvailableGems() : "0"));
            txtGiftsCount.setText((profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0"));
        /*txtGiftsCount.setText(getString(R.string.gifts) + " " + (profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0"));
        txtGiftsCount.setText(getString(R.string.gifts) + " " + (profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0"));*/
        }

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
            R.id.followingsLay, R.id.btnSubscribe, R.id.subscribeLay, R.id.gemsLay, R.id.giftsLay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profileImage: {
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
            }
            break;
            case R.id.btnEdit:
                App.preventMultipleClick(btnEdit);
                Intent profileIntent = new Intent(getActivity(), EditProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                break;
            case R.id.btnRenewal:
            case R.id.renewalLay:
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    Intent primeIntent = new Intent(getActivity(), PrimeActivity.class);
                    primeIntent.putExtra("OnCLick", "ClickHere");
                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(primeIntent);
                }
                break;
            case R.id.btnSettings:
               /* if (App.mInterstitialAd != null && App.mInterstitialAd.isLoaded()) {
                    App.from="Setting";
                    App.mInterstitialAd.show();

                }else {
                    Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(primeIntent);
                }*/
                Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
                primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(primeIntent);
                break;
            case R.id.followersLay:
                Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                followersIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followersIntent);
                break;
            case R.id.followingsLay:
                Intent followingsIntent = new Intent(getActivity(), FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                followingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followingsIntent);
                break;
            case R.id.giftsLay:
             /*   if (GetSet.getGifts() == 0) {
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
                    alertDialog.setContext(getActivity());
                    alertDialog.setMessage(getString(R.string.you_dont_have_any_gifts));
                    alertDialog.show(getChildFragmentManager(), TAG);
                } else {*/
                Intent giftIntent = new Intent(getActivity(), ConvertGiftActivity.class);
                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(giftIntent);
//                }
                break;
            case R.id.gemsLay: {
                Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
                gemsIntent.putExtra("OnCLick", "ClickHere");
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
            }
            break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initProfile();
        getProfile();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        App.makeToast(TAG + ":" + hidden);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void updateProfile() {
        getProfile();
    }
}


