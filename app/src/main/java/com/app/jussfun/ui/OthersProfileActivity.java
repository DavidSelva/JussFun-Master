package com.app.jussfun.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.apprtc.util.AppRTCUtils;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.external.qrgenerator.QRGContents;
import com.app.jussfun.external.qrgenerator.QRGEncoder;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.helper.callback.OnOkClickListener;
import com.app.jussfun.helper.callback.OnReportListener;
import com.app.jussfun.model.FollowRequest;
import com.app.jussfun.model.FollowResponse;
import com.app.jussfun.model.FollowersResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.ui.feed.FeedsActivity;
import com.app.jussfun.ui.feed.ReportAdapter;
import com.app.jussfun.utils.AdminData;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OthersProfileActivity extends BaseFragmentActivity {

    private static String TAG = OthersProfileActivity.class.getSimpleName();
    @BindView(R.id.profileImage)
    RoundedImageView profileImage;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtLocation)
    TextView txtLocation;
    @BindView(R.id.txtFollowersCount)
    TextView txtFollowersCount;
    @BindView(R.id.txtFollowingsCount)
    TextView txtFollowingsCount;
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
    @BindView(R.id.btnFollow)
    Button btnFollow;
    @BindView(R.id.btnReport)
    Button btnReport;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    @BindView(R.id.chatLay)
    RelativeLayout chatLay;
    @BindView(R.id.videoLay)
    RelativeLayout videoLay;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.contactLay)
    LinearLayout contactLay;
    @BindView(R.id.progressLay)
    LinearLayout progressLay;
    ApiInterface apiInterface;
    @BindView(R.id.imageLay)
    RelativeLayout imageLay;
    @BindView(R.id.txtFollowers)
    AppCompatTextView txtFollowers;
    @BindView(R.id.txtFollowings)
    AppCompatTextView txtFollowings;
    @BindView(R.id.btnInterest)
    RelativeLayout btnInterest;
    @BindView(R.id.btnUnInterest)
    RelativeLayout btnUnInterest;
    @BindView(R.id.interestLay)
    LinearLayout interestLay;
    @BindView(R.id.btnShare)
    Button btnShare;
    @BindView(R.id.btnBlock)
    Button btnBlock;
    @BindView(R.id.feedsLay)
    RelativeLayout feedsLay;
    @BindView(R.id.ivFeeds)
    ImageView ivFeeds;
    @BindView(R.id.txtFeedsCount)
    TextView txtFeedsCount;
    @BindView(R.id.txtVideoCount)
    TextView txtVideoCount;
    private String partnerId = "", from = null;
    ProfileResponse othersProfile;
    private RequestOptions profileImageRequest;
    private AppUtils appUtils;
    private QRGEncoder qrgEncoder;
    private String qrImagePath;
    private Bitmap qrBitMap;
    private StorageUtils storageUtils;
    private DBHelper dbHelper;
    private BottomSheetDialog reportDialog;
    private RecyclerView reportsView;
    private ReportAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_others_profile);
        ButterKnife.bind(this);
        showLoading();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (getIntent().hasExtra(Constants.TAG_FROM))
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
        profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .dontAnimate();
        appUtils = new AppUtils(this);
        storageUtils = new StorageUtils(this);
        dbHelper = DBHelper.getInstance(this);

        initView();
        setFromIntent(getIntent());
    }

    private void initView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AppUtils.getStatusBarHeight(getApplicationContext());
        toolbar.setLayoutParams(params);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setImageDrawable(getDrawable(R.drawable.icon_left_back_arrow));
        txtTitle.setText(getString(R.string.profile));
        btnFollow.setVisibility(View.GONE);
        interestLay.setVisibility(View.GONE);
        contactLay.setVisibility(View.GONE);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        if (("" + partnerId).equals(GetSet.getUserId())) {
            btnFollow.setVisibility(View.GONE);
        }
        loadAd();
    }

    private void setFromIntent(Intent intent) {
        if (from != null) {
            switch (from) {
                case Constants.TAG_MESSAGE:
                    txtName.setText("" + intent.getStringExtra(Constants.TAG_PARTNER_NAME));
                    Glide.with(getApplicationContext())
                            .load(Constants.IMAGE_URL + intent.getStringExtra(Constants.TAG_PARTNER_IMAGE))
                            .apply(profileImageRequest)
                            .into(profileImage);
                    getProfile(partnerId);
                    break;
                case Constants.TAG_SEARCH:
                    othersProfile = (ProfileResponse) getIntent().getExtras().get(Constants.TAG_PROFILE_DATA);
                    setProfileData(othersProfile);
                    getProfile(partnerId);
                    break;
                case Constants.TAG_QR_CODE:
                    othersProfile = (ProfileResponse) getIntent().getExtras().get(Constants.TAG_PROFILE_DATA);
                    setProfileData(othersProfile);
                    getProfile(partnerId);
                    break;
                case Constants.TAG_NEARBY:
                    othersProfile = (ProfileResponse) getIntent().getExtras().get(Constants.TAG_PROFILE_DATA);
                    setProfileData(othersProfile);
                    getProfile(partnerId);
                    break;
                default:
                    othersProfile = new ProfileResponse();
                    othersProfile.setName(intent.getStringExtra(Constants.TAG_PARTNER_NAME));
                    othersProfile.setAge(intent.getStringExtra(Constants.TAG_AGE));
                    othersProfile.setUserImage(intent.getStringExtra(Constants.TAG_PARTNER_IMAGE));
                    othersProfile.setGender(intent.getStringExtra(Constants.TAG_GENDER));
                    othersProfile.setBlockedByMe(intent.getStringExtra(Constants.TAG_BLOCKED_BY_ME));
                    othersProfile.setLocation(intent.getStringExtra(Constants.TAG_LOCATION));
                    othersProfile.setFollow(intent.getStringExtra(Constants.TAG_FOLLOW));
                    othersProfile.setPrivacyAge(intent.getStringExtra(Constants.TAG_PRIVACY_AGE));
                    othersProfile.setPrivacyContactMe(intent.getStringExtra(Constants.TAG_PRIVACY_CONTACT_ME));
                    othersProfile.setFollowers(intent.getStringExtra(Constants.TAG_FOLLOWERS));
                    othersProfile.setFollowings(intent.getStringExtra(Constants.TAG_FOLLOWINGS));
                    othersProfile.setPremiumMember(intent.getStringExtra(Constants.TAG_PREMIUM_MEBER));
                    if (from.equals(Constants.TAG_FRIENDS)) {
                        contactLay.setVisibility(othersProfile.getPrivacyContactMe().equals(Constants.TAG_TRUE) ? View.GONE : View.VISIBLE);
                    }
                    setProfileData(othersProfile);
                    getProfile(partnerId);
                    break;
            }
        } else {
            if (intent.hasExtra(Constants.TAG_PARTNER_ID)) {
                othersProfile = new ProfileResponse();
                othersProfile.setName(intent.getStringExtra(Constants.TAG_PARTNER_NAME));
                othersProfile.setAge(intent.getStringExtra(Constants.TAG_AGE));
                othersProfile.setUserImage(intent.getStringExtra(Constants.TAG_PARTNER_IMAGE));
                othersProfile.setGender(intent.getStringExtra(Constants.TAG_GENDER));
                othersProfile.setBlockedByMe(intent.getStringExtra(Constants.TAG_BLOCKED_BY_ME));
                othersProfile.setLocation(intent.getStringExtra(Constants.TAG_LOCATION));
                othersProfile.setFollow(intent.getStringExtra(Constants.TAG_FOLLOW));
                othersProfile.setPrivacyAge(intent.getStringExtra(Constants.TAG_PRIVACY_AGE));
                othersProfile.setPrivacyContactMe(intent.getStringExtra(Constants.TAG_PRIVACY_CONTACT_ME));
                othersProfile.setFollowers(intent.getStringExtra(Constants.TAG_FOLLOWERS));
                othersProfile.setFollowings(intent.getStringExtra(Constants.TAG_FOLLOWINGS));
                othersProfile.setPremiumMember(intent.getStringExtra(Constants.TAG_PREMIUM_MEBER));
                setProfileData(othersProfile);
                getProfile(partnerId);
            }
        }
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            BannerAdUtils.getInstance(this).loadAd(TAG, adView);
        }
    }

    private void getProfile(String userId) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(userId);
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        othersProfile = profile;
                        setProfileData(profile);
                        setFollowData(profile);
                    } else {
                        hideLoading();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    hideLoading();
                }
            });
        }
    }

    private void setProfileData(ProfileResponse profile) {
        if (("" + profile.getPrivacyAge()).equals(Constants.TAG_TRUE)) {
            txtName.setText("" + profile.getName());
        } else {
            txtName.setText(profile.getName() + ", " + profile.getAge());
        }
        txtFollowings.setText(getString(R.string.followings));
        txtFollowers.setText(getString(R.string.followers));

        txtFollowingsCount.setText("" + profile.getFriends());
        txtFollowersCount.setText("" + profile.getInterests());
        txtFeedsCount.setText(profile.getFeedCount());

        premiumImage.setVisibility(profile.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);
        txtLocation.setText(AppUtils.formatWord(profile.getLocation()));

        Glide.with(getApplicationContext())
                .load(Constants.IMAGE_URL + profile.getUserImage())
                .apply(profileImageRequest)
                .into(profileImage);

        genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                getDrawable(R.drawable.men) : getDrawable(R.drawable.women));

    }

    private void setFollowData(ProfileResponse profile) {
        btnShare.setVisibility(View.VISIBLE);
        if (profile.getIsFriend()) {
            contactLay.setVisibility(profile.getPrivacyContactMe().equals(Constants.TAG_TRUE) ? View.GONE : View.VISIBLE);
        } else {
            if (profile.getInterestedByMe()) {
                interestLay.setVisibility(View.GONE);
            } else {
                interestLay.setVisibility(View.VISIBLE);
                if (from != null) {
                    if (from.equals(Constants.TAG_NEARBY) || from.equals(Constants.TAG_INTEREST)) {
                        btnInterest.setVisibility(View.VISIBLE);
                        btnUnInterest.setVisibility(View.VISIBLE);
                    } else {
                        btnInterest.setVisibility(View.VISIBLE);
                        btnUnInterest.setVisibility(View.GONE);
                    }
                }
            }
        }
        if (profile.getBlockedByMe().equals(Constants.TAG_TRUE)) {
            btnBlock.setText(getString(R.string.unblock));
        } else {
            btnBlock.setText(getString(R.string.block));
        }


        btnBlock.setVisibility(View.VISIBLE);
        if (profile.getBlockedByMe() != null && !TextUtils.isEmpty(profile.getBlockedByMe())) {
            if (profile.getBlockedByMe().equals(Constants.TAG_TRUE)) {
                btnBlock.setText(getString(R.string.unblock));
            } else {
                btnBlock.setText(getString(R.string.block));
            }
        } else {
            btnBlock.setVisibility(View.GONE);
        }

        hideLoading();
    }

    private void setFollowButton(boolean status) {
        if (status) {
            btnFollow.setText(getString(R.string.unfollow));
            btnFollow.setBackground(getResources().getDrawable(R.drawable.rounded_solid_white));
            btnFollow.setTextColor(getResources().getColor(R.color.colorPrimaryText));
        } else {
            btnFollow.setText(getString(R.string.follow));
            btnFollow.setBackground(getResources().getDrawable(R.drawable.rounded_corner_primary));
            btnFollow.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    @OnClick({R.id.profileImage, R.id.btnSettings, R.id.followersLay, R.id.followingsLay,
            R.id.btnFollow, R.id.btnBack, R.id.chatLay, R.id.videoLay, R.id.feedsLay, R.id.videosLay,
            R.id.btnInterest, R.id.btnUnInterest, R.id.btnBlock, R.id.btnShare, R.id.btnReport})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profileImage:
                if (othersProfile != null) {
                    Intent profile = new Intent(getApplicationContext(), ImageViewActivity.class);
                    profile.putExtra(Constants.TAG_USER_IMAGE, othersProfile.getUserImage());
                    profile.putExtra(Constants.TAG_FROM, Constants.TAG_OTHER_PROFILE);
                    startActivityForResult(profile, Constants.INTENT_REQUEST_CODE);
                    overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
                }
                break;
            case R.id.btnSettings:
                break;
            case R.id.followersLay:
                App.preventMultipleClick(followersLay);
                Intent followersIntent = new Intent(getApplicationContext(), FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, partnerId);
                startActivity(followersIntent);
                break;
            case R.id.followingsLay:
                App.preventMultipleClick(followingsLay);
                Intent followingsIntent = new Intent(getApplicationContext(), FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, partnerId);
                startActivity(followingsIntent);
                break;
            case R.id.btnFollow:
                App.preventMultipleClick(btnFollow);
                followUnFollowUser(partnerId);
                break;
            case R.id.btnReport: {
                App.preventMultipleClick(btnReport);
                if (btnReport.getText().toString().equals(getString(R.string.report_user))) {
                    openReportDialog();
                } else {
                    App.makeToast(getString(R.string.undo_report_successfully));
                }
            }
            break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.chatLay:
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                chatIntent.putExtra(Constants.TAG_PARTNER_ID, partnerId);
                chatIntent.putExtra(Constants.TAG_PARTNER_NAME, othersProfile.getName());
                chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, othersProfile.getUserImage());
                chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, othersProfile.getBlockedByMe());
                startActivity(chatIntent);
                break;
            case R.id.videoLay:
                if (othersProfile.getBlockedByMe().equals(Constants.TAG_FALSE)) {
                    if (GetSet.getGems() >= AdminData.videoCallsGems) {
                        if (NetworkReceiver.isConnected()) {
                            App.preventMultipleClick(videoLay);
                            AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                            Intent callIntent = appRTCUtils.connectToRoom(partnerId, Constants.TAG_SEND, Constants.TAG_VIDEO);
                            callIntent.putExtra(Constants.TAG_USER_NAME, othersProfile.getName());
                            callIntent.putExtra(Constants.TAG_USER_IMAGE, othersProfile.getUserImage());
                            callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            startActivity(callIntent);
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                    }
                } else {
                    App.makeToast(getString(R.string.unblock_description));
                }
                break;
            case R.id.btnInterest: {
                App.preventMultipleClick(btnInterest);
                appUtils.onInterestClicked(btnInterest);
                String profile = new Gson().toJson(othersProfile);
                FollowersResponse.FollowersList userProfile = new Gson().fromJson(profile, FollowersResponse.FollowersList.class);
                interestOnUser(partnerId, 1, userProfile);
            }
            break;
            case R.id.btnUnInterest: {
                App.preventMultipleClick(btnUnInterest);
                appUtils.onInterestClicked(btnUnInterest);
                String profile = new Gson().toJson(othersProfile);
                FollowersResponse.FollowersList userProfile = new Gson().fromJson(profile, FollowersResponse.FollowersList.class);
                interestOnUser(partnerId, 0, userProfile);
            }
            break;
            case R.id.btnBlock:
                App.preventMultipleClick(btnBlock);
                blockUnBlockUser(partnerId);
                break;
            case R.id.btnShare:
                btnShare.setEnabled(false);
                new GetQRCodeTask().execute();
                break;
            case R.id.feedsLay: {
                Intent feedIntent = new Intent(this, FeedsActivity.class);
                feedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                feedIntent.putExtra(Constants.TAG_USER_ID, partnerId);
                feedIntent.putExtra(Constants.TAG_TYPE, Constants.TAG_PHOTO);
                startActivity(feedIntent);
            }
            break;
            case R.id.videosLay: {
                Intent feedIntent = new Intent(this, FeedsActivity.class);
                feedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                feedIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                feedIntent.putExtra(Constants.TAG_TYPE, Constants.TAG_VIDEO);
                startActivity(feedIntent);
            }
            break;
            default:
                break;
        }
    }

    private void followUnFollowUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(partnerId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (othersProfile.getFollow().equals(Constants.TAG_TRUE)) {
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);
            } else {
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            }
            Call<FollowResponse> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<FollowResponse>() {
                @Override
                public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                    FollowResponse followResponse = response.body();
                    if (followResponse.getStatus().equals(Constants.TAG_TRUE)) {
                        if (followRequest.getType().equals(Constants.TAG_FOLLOW_USER)) {
//                            App.makeToast(getString(R.string.followed_successfully));
                            othersProfile.setFollow("" + Constants.TAG_TRUE);
                            setFollowButton(true);
                        } else {
//                            App.makeToast(getString(R.string.unfollowed_successfully));
                            othersProfile.setFollow("" + Constants.TAG_FALSE);
                            setFollowButton(false);
                        }
                    }
                    getProfile(partnerId);
                }

                @Override
                public void onFailure(Call<FollowResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void interestOnUser(String partnerId, int isInterested, FollowersResponse.FollowersList userProfile) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_INTEREST_USER_ID, partnerId);
            requestMap.put(Constants.TAG_INTERESTED, "" + isInterested);
            Call<Map<String, String>> call = apiInterface.interestOnUser(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            interestLay.setVisibility(View.GONE);
                            if (responseMap.get(Constants.TAG_FRIEND).equals(Constants.TAG_TRUE)) {
                                showMatchDialog(userProfile);
                                contactLay.setVisibility(userProfile.getPrivacyContactMe().equals(Constants.TAG_TRUE) ?
                                        View.GONE : View.VISIBLE);
                            } else {
                                contactLay.setVisibility(View.GONE);
                                if (isInterested == 1) {
                                    App.makeToast(getString(R.string.liked_successfully));
                                } else {
                                    App.makeToast(getString(R.string.declined_successfully));
                                }
                            }

                            if (from != null) {
                                switch (from) {
                                    case Constants.TAG_NEARBY:
                                        int position = getIntent().getIntExtra(Constants.TAG_POSITION, 0);
                                        NearByUsersFragment.nearByUsersList.remove(position);
                                        NearByUsersFragment.nearByAdapter.notifyItemRemoved(position);
                                        break;
                                    case Constants.TAG_INTEREST:
                                        FollowersActivity.hasInterestChange = true;
                                        FollowersActivity.hasFriendChange = true;
                                        break;
                                    case Constants.TAG_FRIENDS:
                                        FollowersActivity.hasFriendChange = true;
                                        break;
                                }
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void showMatchDialog(FollowersResponse.FollowersList userProfile) {
        DialogMatch dialogMatch = new DialogMatch();
        dialogMatch.setContext(this);
        dialogMatch.setPartnerData(userProfile);
        dialogMatch.setCallBack(new OnOkClickListener() {
            @Override
            public void onOkClicked(Object object) {
                boolean isChat = (boolean) object;
                if (isChat) {
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Constants.TAG_PARTNER_ID, userProfile.getUserId());
                    intent.putExtra(Constants.TAG_PARTNER_NAME, userProfile.getName());
                    intent.putExtra(Constants.TAG_PARTNER_IMAGE, userProfile.getUserImage());
                    startActivity(intent);
                    dialogMatch.dismissAllowingStateLoss();
                } else {
                    dialogMatch.dismissAllowingStateLoss();
                }
            }
        });
        dialogMatch.show(getSupportFragmentManager(), TAG);
    }

    private void blockUnBlockUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, partnerId);
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (from != null && from.equals(Constants.TAG_BLOCKED)) {
                                BlockedUsersActivity.hasBlockChanges = true;
                            }
                            String chatId = GetSet.getUserId() + othersProfile.getUserId();
                            if (responseMap.get(Constants.TAG_BLOCK_STATUS).equals("1")) {
                                othersProfile.setBlockedByMe(Constants.TAG_TRUE);
                                btnBlock.setText(getString(R.string.unblock));
                                App.makeToast(getString(R.string.user_blocked_successfully));
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, othersProfile.getUserId(), othersProfile.getName(), othersProfile.getUserImage(), Constants.TAG_TRUE);
                                }
                            } else {
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, othersProfile.getUserId(), othersProfile.getName(), othersProfile.getUserImage(), Constants.TAG_FALSE);
                                }
                                othersProfile.setBlockedByMe(Constants.TAG_FALSE);
                                btnBlock.setText(getString(R.string.block));
                                App.makeToast(getString(R.string.unblocked_successfully));
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

    private void openReportDialog() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(this, R.style.Bottom_FilterDialog); // Style here
        reportDialog.setContentView(sheetView);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
        int maxHeight = (AppUtils.getDisplayHeight(this) * 80) / 100;
        mBehavior.setPeekHeight(maxHeight);
        sheetView.requestLayout();
        reportsView = sheetView.findViewById(R.id.reportView);
        reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        loadReports();
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(this, AdminData.reportList, new OnReportListener() {
            @Override
            public void onReportSend(Object o) {
                App.makeToast(getString(R.string.reported_successfully));
//                sendReport((String) o);
            }
        });
        reportsView.setLayoutManager(new LinearLayoutManager(this));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetQRCodeTask extends AsyncTask<Void, Void, String> {
        String profileData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.TAG_USER_ID, AppUtils.encryptMessage(othersProfile.getUserId()));
                jsonObject.put(Constants.TAG_NAME, AppUtils.encryptMessage(othersProfile.getName()));
                jsonObject.put(Constants.TAG_USER_IMAGE, othersProfile.getUserImage());
                jsonObject.put(Constants.TAG_AGE, Integer.parseInt(othersProfile.getAge()));
                jsonObject.put(Constants.TAG_GENDER, othersProfile.getGender());
                jsonObject.put(Constants.TAG_LOCATION, othersProfile.getLocation());
                jsonObject.put(Constants.TAG_PREMIUM_MEBER, Boolean.parseBoolean(othersProfile.getPremiumMember()));
                jsonObject.put(Constants.TAG_PRIVACY_CONTACT_ME, Boolean.parseBoolean(othersProfile.getPrivacyContactMe()));
                jsonObject.put(Constants.TAG_PRIVACY_AGE, Boolean.parseBoolean(othersProfile.getPrivacyAge()));

                jsonObject.put(Constants.TAG_APP_NAME, getResources().getString(R.string.app_name));
                Log.i(TAG, "app_nameonPreExecute: " + jsonObject);
                profileData = "" + jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            profileData = new Gson().toJson(MainActivity.profileResponse);
//            profileData = AppUtils.encryptMessage(profileData);
            Log.i(TAG, "onPreExecute: " + profileData);
        }

        @Override
        protected String doInBackground(Void... voids) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = Math.min(width, height);
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    profileData, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            Bitmap bitmap = qrgEncoder.getBitmap();
            if (bitmap != null) {
                File file = storageUtils.getTempFile(OthersProfileActivity.this, System.currentTimeMillis() + "jpg");
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    //getting the logo
                    Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.ic_app_logo_transparent);
                    overlay = Bitmap.createScaledBitmap(overlay, AppUtils.dpToPx(OthersProfileActivity.this, 60),
                            AppUtils.dpToPx(OthersProfileActivity.this, 60), false);
                    qrBitMap = mergeQRWithLogo(overlay, bitmap);
                    File tempFile = storageUtils.saveToCacheDir(qrBitMap, System.currentTimeMillis() + ".jpg");
                    return tempFile != null ? tempFile.getAbsolutePath() : null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String filePath) {
            super.onPostExecute(filePath);
            hideLoading();
            btnShare.setEnabled(true);
            if (filePath != null) {
                qrImagePath = filePath;
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpg");
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(OthersProfileActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(qrImagePath));
                } else {
                    uri = Uri.fromFile(new File(qrImagePath));
                }
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.checkout_this_profile_from) + " " + getString(R.string.app_name) + ": " +
                        "https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=" + LocaleManager.getLanguageCode(OthersProfileActivity.this));
                startActivity(Intent.createChooser(share, "Share Image"));
            } else {
                qrImagePath = null;
            }
        }
    }

    public Bitmap mergeQRWithLogo(Bitmap overlay, Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth - overlay.getWidth()) / 2;
        int centreY = (canvasHeight - overlay.getHeight()) / 2;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String userId) {
        if (partnerId.equals(userId)) {
            getProfile(partnerId);
        }
    }

    public void showLoading() {
        /*Disable touch options*/
        progressLay.setVisibility(View.VISIBLE);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }

    public void hideLoading() {
        /*Enable touch options*/
        progressLay.setVisibility(View.GONE);
        /*getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }
}


