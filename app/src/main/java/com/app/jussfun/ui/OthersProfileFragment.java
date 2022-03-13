package com.app.jussfun.ui;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.app.jussfun.BuildConfig;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.db.DBHelper;
import com.app.jussfun.external.qrgenerator.QRGContents;
import com.app.jussfun.external.qrgenerator.QRGEncoder;
import com.app.jussfun.helper.BannerAdUtils;
import com.app.jussfun.helper.LocaleManager;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.StorageUtils;
import com.app.jussfun.helper.callback.FollowUpdatedListener;
import com.app.jussfun.model.FollowRequest;
import com.app.jussfun.model.FollowResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileRequest;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.ui.feed.FeedsActivity;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.AppUtils;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.makeramen.roundedimageview.RoundedImageView;

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

public class OthersProfileFragment extends Fragment {

    private static String TAG = OthersProfileFragment.class.getSimpleName();
    @BindView(R.id.btnFollow)
    Button btnFollow;
    @BindView(R.id.premiumImage)
    ImageView premiumImage;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.contactLay)
    LinearLayout contactLay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;

    @BindView(R.id.txtFollowers)
    AppCompatTextView txtFollowers;
    @BindView(R.id.followersLay)
    RelativeLayout followersLay;
    @BindView(R.id.txtFollowings)
    AppCompatTextView txtFollowings;
    @BindView(R.id.chatLay)
    RelativeLayout chatLay;
    @BindView(R.id.videoLay)
    RelativeLayout videoLay;
    @BindView(R.id.btnInterest)
    RelativeLayout btnInterest;
    @BindView(R.id.btnUnInterest)
    RelativeLayout btnUnInterest;
    @BindView(R.id.interestLay)
    LinearLayout interestLay;
    @BindView(R.id.progressLay)
    LinearLayout progressLay;

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
    @BindView(R.id.followingsLay)
    RelativeLayout followingsLay;
    @BindView(R.id.feedsLay)
    RelativeLayout feedsLay;
    @BindView(R.id.txtFeedsCount)
    TextView txtFeedsCount;
    @BindView(R.id.btnBlock)
    Button btnBlock;
    @BindView(R.id.btnShare)
    Button btnShare;

    private Bundle bundle;
    ApiInterface apiInterface;
    private String partnerId = "";
    ProfileResponse othersProfile;
    private Context context;
    private FollowUpdatedListener updateListener;
    private DBHelper dbHelper;
    private QRGEncoder qrgEncoder;
    private String qrImagePath;
    private Bitmap qrBitMap;
    private AppUtils appUtils;
    private StorageUtils storageUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.activity_others_profile, container, false);
        ButterKnife.bind(this, rootView);
        bundle = getArguments();
        partnerId = bundle.getString(Constants.TAG_PARTNER_ID);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(getActivity());
        storageUtils = new StorageUtils(getActivity());
        dbHelper = DBHelper.getInstance(getActivity());

        btnFollow.setVisibility(View.GONE);
        interestLay.setVisibility(View.GONE);
        contactLay.setVisibility(View.GONE);

        loadAd();
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setVisibility(View.INVISIBLE);
        setProfileFromBundle(bundle);
        return rootView;


    }

    private void loadAd() {
        BannerAdUtils.getInstance(context).loadAd(TAG, adView);
    }


    private void setProfileFromBundle(Bundle bundle) {
        if (bundle != null && !bundle.containsKey(Constants.TAG_USER_ID)) {
            ProfileResponse profile = new ProfileResponse();
            profile.setUserId(partnerId);
            profile.setName(bundle.getString(Constants.TAG_NAME));
            profile.setUserImage(bundle.getString(Constants.TAG_USER_IMAGE));
            profile.setAge(bundle.getString(Constants.TAG_AGE));
            profile.setGender(bundle.getString(Constants.TAG_GENDER));
            profile.setDob(bundle.getString(Constants.TAG_DOB));
            profile.setPremiumMember(bundle.getString(Constants.TAG_PREMIUM_MEBER));
            profile.setLocation(bundle.getString(Constants.TAG_LOCATION));
            profile.setFollowers(bundle.getString(Constants.TAG_FOLLOWERS));
            profile.setFollowings(bundle.getString(Constants.TAG_FOLLOWINGS));
            profile.setFollow(bundle.getString(Constants.TAG_FOLLOW));
            profile.setPrivacyAge(bundle.getString(Constants.TAG_PRIVACY_AGE));
            profile.setPrivacyContactMe(bundle.getString(Constants.TAG_PRIVACY_CONTACT_ME));
            setProfile(profile);
        }
    }

    private void getProfile(String userId) {
        partnerId = userId;
        ProfileRequest request = new ProfileRequest();
        request.setUserId(GetSet.getUserId());
        request.setProfileId(userId);
        Call<ProfileResponse> call = apiInterface.getProfile(request);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful()) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        othersProfile = profile;
                        setProfile(profile);
                        setFollowData(profile);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {

            }
        });
    }

    private void setProfile(ProfileResponse profile) {
        if (!profile.getUserId().equals(GetSet.getUserId())) {
            if (profile.getPrivacyAge().equals(Constants.TAG_TRUE)) {
                txtName.setText(profile.getName());
            } else {
                txtName.setText(profile.getName() + ", " + profile.getAge());
            }
            txtFollowings.setText(getString(R.string.followings));
            txtFollowers.setText(getString(R.string.friends));

            txtFollowingsCount.setText("" + profile.getFriends());
            txtFollowersCount.setText("" + profile.getInterests());
            txtFeedsCount.setText(profile.getFeedCount());

            premiumImage.setVisibility(profile.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);

            Glide.with(OthersProfileFragment.this)
                    .load(Constants.IMAGE_URL + profile.getUserImage())
                    .apply(App.getProfileImageRequest())
                    .into(profileImage);

            genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                    context.getResources().getDrawable(R.drawable.men) : context.getResources().getDrawable(R.drawable.women));
            txtLocation.setText(AppUtils.formatWord(profile.getLocation()));
        }
    }

    private void setFollowData(ProfileResponse profile) {
        if (profile.getIsFriend()) {
            contactLay.setVisibility(profile.getPrivacyContactMe().equals(Constants.TAG_TRUE) ? View.GONE : View.VISIBLE);
        } else {
            if (profile.getInterestedByMe()) {
                interestLay.setVisibility(View.GONE);
            } else {
                interestLay.setVisibility(View.VISIBLE);
                btnInterest.setVisibility(View.VISIBLE);
                btnUnInterest.setVisibility(View.GONE);
            }
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
        btnShare.setVisibility(View.VISIBLE);
    }

    private void setFollowButton(boolean status) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    @OnClick({R.id.profileImage, R.id.btnSettings, R.id.followersLay, R.id.followingsLay, R.id.btnFollow, R.id.feedsLay,
            R.id.btnInterest, R.id.btnUnInterest, R.id.btnBlock, R.id.btnShare, R.id.chatLay, R.id.videoLay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profileImage:
                /*Intent profile = new Intent(getActivity(), ImageViewActivity.class);
                profile.putExtra(Constants.TAG_USER_IMAGE, othersProfile != null ? othersProfile.getUserImage() : "");
                profile.putExtra(Constants.EDIT, false);
                startActivityForResult(profile, Constants.INTENT_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);*/
                App.makeToast(getString(R.string.already_in_live));
                break;
            case R.id.btnSettings:
                App.makeToast(getString(R.string.already_in_live));
                break;
            case R.id.chatLay:
                App.makeToast(getString(R.string.already_in_live));
                break;
            case R.id.videoLay:
                App.makeToast(getString(R.string.already_in_live));
                break;
            case R.id.followersLay:
                App.makeToast(getString(R.string.already_in_live));
                break;
            case R.id.followingsLay:
                App.makeToast(getString(R.string.already_in_live));
                break;
            case R.id.btnFollow:
                App.preventMultipleClick(btnFollow);
                followUnFollowUser(partnerId);
                break;
            case R.id.btnInterest: {
                App.preventMultipleClick(btnInterest);
                appUtils.onInterestClicked(btnInterest);
                interestOnUser(partnerId, 1);
            }
            break;
            case R.id.btnUnInterest: {
                App.preventMultipleClick(btnUnInterest);
                appUtils.onInterestClicked(btnUnInterest);
                interestOnUser(partnerId, 0);
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
                Intent feedIntent = new Intent(getActivity(), FeedsActivity.class);
                feedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                feedIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(feedIntent);
            }
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
                            othersProfile.setFollow(Constants.TAG_TRUE);
                            if (updateListener != null) {
                                updateListener.onFollowUpdated(othersProfile, -1);
                            }
                            setFollowButton(true);
//                            App.makeToast(getString(R.string.followed_successfully));
                        } else {
                            othersProfile.setFollow(Constants.TAG_FALSE);
                            if (updateListener != null) {
                                updateListener.onFollowUpdated(othersProfile, -1);
                            }
                            setFollowButton(false);
//                            App.makeToast(getString(R.string.unfollowed_successfully));
                        }
                    }
                }

                @Override
                public void onFailure(Call<FollowResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void interestOnUser(String partnerId, int isInterested) {
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
                                othersProfile.setFriend(true);
                                App.makeToast(getString(R.string.both_are_became_friends_now));
                                contactLay.setVisibility(othersProfile.getPrivacyContactMe().equals(Constants.TAG_TRUE) ?
                                        View.GONE : View.VISIBLE);
                            } else {
                                othersProfile.setFriend(false);
                                contactLay.setVisibility(View.GONE);
                                if (isInterested == 1) {
                                    App.makeToast(getString(R.string.liked_successfully));
                                } else {
                                    App.makeToast(getString(R.string.declined_successfully));
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
                        String chatId = GetSet.getUserId() + othersProfile.getUserId();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
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
                                othersProfile.setBlockedByMe(Constants.TAG_FALSE);
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, othersProfile.getUserId(), othersProfile.getName(), othersProfile.getUserImage(), Constants.TAG_FALSE);
                                }
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

    @Override
    public void onResume() {
        super.onResume();
        getProfile(partnerId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetQRCodeTask extends AsyncTask<Void, Void, String> {
        String profileData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            WindowManager manager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
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
                File file = storageUtils.getTempFile(getActivity(), System.currentTimeMillis() + ".jpg");
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    //getting the logo
                    Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.qr_code_logo);
                    overlay = Bitmap.createScaledBitmap(overlay, AppUtils.dpToPx(getActivity(), 60),
                            AppUtils.dpToPx(getActivity(), 60), false);
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
            btnShare.setEnabled(true);
            if (filePath != null) {
                qrImagePath = filePath;
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpg");
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(qrImagePath));
                } else {
                    uri = Uri.fromFile(new File(qrImagePath));
                }
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.checkout_this_profile_from) + " " + getString(R.string.app_name) + ": " +
                        "https://play.google.com/store/apps/details?id=" + context.getPackageName() + "&hl=" + LocaleManager.getLanguageCode(context));
                share.putExtra(Intent.EXTRA_STREAM, uri);
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

    public void updateFragment(String partnerId) {
        getProfile(partnerId);
    }

    public void setFollowUpdatedListener(FollowUpdatedListener listener) {
        this.updateListener = listener;
    }
}


