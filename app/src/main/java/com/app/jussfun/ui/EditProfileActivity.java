package com.app.jussfun.ui;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
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
import com.google.android.gms.ads.AdView;
import com.google.android.material.card.MaterialCardView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditProfileActivity extends BaseFragmentActivity {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    @BindView(R.id.profileImage)
    ImageView profileImage;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    AppCompatTextView txtTitle;
    @BindView(R.id.txtSubTitle)
    AppCompatTextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtPrimeTitle)
    AppCompatTextView txtPrimeTitle;
    @BindView(R.id.txtPrice)
    AppCompatTextView txtPrice;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.primeBgLay)
    RelativeLayout primeBgLay;
    @BindView(R.id.subscribeLay)
    RelativeLayout subscribeLay;
    @BindView(R.id.txtRenewalTitle)
    AppCompatTextView txtRenewalTitle;
    @BindView(R.id.txtRenewalDes)
    AppCompatTextView txtRenewalDes;
    @BindView(R.id.btnRenewal)
    AppCompatButton btnRenewal;
    @BindView(R.id.renewalBgLay)
    RelativeLayout renewalBgLay;
    @BindView(R.id.renewalLay)
    RelativeLayout renewalLay;
    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.txtUserName)
    AppCompatTextView txtUserName;
    @BindView(R.id.txtGender)
    AppCompatTextView txtGender;
    @BindView(R.id.txtDob)
    AppCompatTextView txtDob;
    @BindView(R.id.txtLocation)
    AppCompatTextView txtLocation;
    @BindView(R.id.txtGiftsCount)
    AppCompatTextView txtGiftsCount;
    @BindView(R.id.edtPayPal)
    AppCompatEditText edtPayPal;
    @BindView(R.id.payPalLay)
    MaterialCardView payPalLay;
    @BindView(R.id.edtBankDetails)
    AppCompatEditText edtBankDetails;
    @BindView(R.id.edtBankLay)
    LinearLayout edtBankLay;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;

    private ApiInterface apiInterface;
    private ProfileResponse profileResponse;
    private AppUtils appUtils;
    RequestOptions profileImageRequest = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .dontAnimate();
    private String strLocation;


    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        profileResponse = (ProfileResponse) getIntent().getSerializableExtra(Constants.TAG_PROFILE_DATA);
        initView();
        setProfile(profileResponse);
    }

    private void initView() {

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        appUtils = new AppUtils(this);
        edtName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
//        edtPayPal.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, AppUtils.EMOJI_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        edtPayPal.setFilters(new InputFilter[]{AppUtils.EMOJI_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});

        edtName.setSelection(edtName.getText().length());
        edtName.requestFocus();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setImageDrawable(getDrawable(R.drawable.arrow_w_l));
        loadAd();
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }


    @OnClick({R.id.btnSave, R.id.btnBack, R.id.profileImage, R.id.btnSubscribe, R.id.subscribeLay,
            R.id.btnRenewal, R.id.renewalLay, R.id.txtLocation, R.id.locationLay, R.id.bankLay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                if (TextUtils.isEmpty(edtName.getText().toString())) {
                    edtName.setError(getString(R.string.enter_name));
                } else if (TextUtils.isEmpty(txtLocation.getText().toString())) {
                    txtLocation.setError(getString(R.string.select_location));
                } else if (TextUtils.isEmpty(edtPayPal.getText()) || !edtPayPal.getText().toString().matches(emailPattern)) {
                    edtPayPal.setError(getString(R.string.verify_mail));
                } else {
                    txtLocation.setError(null);
                    hideKeyboard(EditProfileActivity.this);
                    saveProfile();
                }
                break;
            case R.id.btnBack:
                hideKeyboard(EditProfileActivity.this);
                onBackPressed();
                break;
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
            case R.id.btnRenewal:
            case R.id.renewalLay:
                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                    prime.putExtra("OnCLick", "ClickHere");
                    startActivity(prime);
                }
                break;
            case R.id.profileImage:
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
                break;
            case R.id.txtLocation:
            case R.id.locationLay:
                Intent location = new Intent(this, LocationFilterActivity.class);
                location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                location.putExtra(Constants.TAG_LOCATION, strLocation != null ? strLocation : "");
                startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
                break;
            case R.id.bankLay: {
                Intent bankIntent = new Intent(this, BankDetailsActivity.class);
                bankIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(bankIntent);
            }
            break;
        }
    }

    private void saveProfile() {
        if (NetworkReceiver.isConnected()) {
//            showLoading();
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setName(edtName.getText().toString());
            request.setPayPalId("" + edtPayPal.getText());
            request.setLocation("" + txtLocation.getText());
            request.setLocation("" + txtLocation.getText());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
//                    hideLoading();
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.profile_updated_successfully));
                        MainActivity.profileResponse = profile;
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
                        GetSet.setFriendsCount(profile.getFriends());
                        GetSet.setUnlocksLeft(profile.getUnlocksLeft());
                        GetSet.setInterestsCount(profile.getInterests());
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(profile.getAvailableGifts()) : 0);
                        GetSet.setGems(profile.getAvailableGems() != null ? Long.parseLong(profile.getAvailableGems()) : 0);
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        GetSet.setPrivacyAge(profile.getPrivacyAge());
                        GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());
                        GetSet.setShowNotification(profile.getShowNotification());
                        GetSet.setFollowNotification(profile.getFollowNotification());
                        GetSet.setChatNotification(profile.getChatNotification());
                        GetSet.setInterestNotification(profile.getInterestNotification());
                        GetSet.setGiftEarnings(profile.getGiftEarnings());
                        GetSet.setInterestsCount(profile.getInterests());
                        GetSet.setFriendsCount(profile.getFriends());

                        GetSet.setGiftCoversionEarnings(profile.getGiftCoversionEarnings());
                        GetSet.setGiftConversionValue(profile.getGiftConversionValue());

                        GetSet.setPaypal_id(profile.getPaypalId());

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
                        SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                        SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());

                        SharedPref.putString(SharedPref.GIFT_CONVERSION_EARNINGS, GetSet.getGiftCoversionEarnings());
                        SharedPref.putString(SharedPref.GIFT_CONVERSION_VALUE, GetSet.getGiftConversionValue());
                        SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_PROFILE_DATA, profile);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
//                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void setProfile(ProfileResponse profile) {
        edtName.setText(profile.getName());
        Glide.with(getApplicationContext())
                .load(Constants.IMAGE_URL + profile.getUserImage())
                .apply(profileImageRequest)
                .into(profileImage);

        txtGender.setText(profile.getGender());
        txtLocation.setText(profile.getLocation());
        strLocation = profile.getLocation();
        txtUserName.setText(profile.getUserName());
        txtDob.setText(profile.getDob());
        edtPayPal.setText(profile.getPaypalId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {
            profileResponse.setUserImage(GetSet.getUserImage());
            Glide.with(getApplicationContext())
                    .load(Constants.IMAGE_URL + GetSet.getUserImage())
                    .apply(profileImageRequest)
                    .into(profileImage);
        } else if (resultCode == Activity.RESULT_OK && requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (data != null) {
                strLocation = data.getStringExtra(Constants.TAG_LOCATION);
            } else {
                strLocation = null;
            }
            txtLocation.setText(strLocation != null ? strLocation : "");
        }
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(GONE);
    }

    private void initPrimeView() {
        /*Init Prime View*/
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            renewalLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(GONE);
        } else {
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if ((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(EditProfileActivity.this);
        }
        return super.dispatchTouchEvent(ev);
    } */

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
        initPrimeView();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

}
