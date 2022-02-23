/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.app.jussfun.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.app.jussfun.R;
import com.app.jussfun.base.App;
import com.app.jussfun.external.shimmer.ShimmerFrameLayout;
import com.app.jussfun.helper.NetworkReceiver;
import com.app.jussfun.helper.callback.ProfileUpdatedListener;
import com.app.jussfun.model.FollowRequest;
import com.app.jussfun.model.FollowResponse;
import com.app.jussfun.model.GetSet;
import com.app.jussfun.model.ProfileResponse;
import com.app.jussfun.utils.ApiClient;
import com.app.jussfun.utils.ApiInterface;
import com.app.jussfun.utils.Constants;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for call control.
 */
public class UserBottomFragment extends Fragment {

    private static final String TAG = UserBottomFragment.class.getSimpleName();
    @BindView(R.id.lottieImage)
    LottieAnimationView lottieImage;
    @BindView(R.id.shimmerLayout)
    ShimmerFrameLayout shimmerLayout;
    @BindView(R.id.bottomLay)
    LinearLayout bottomLay;
    @BindView(R.id.bottomUserImage)
    RoundedImageView bottomUserImage;
    @BindView(R.id.bottomTxtName)
    TextView bottomTxtName;
    @BindView(R.id.bottomTxtAge)
    TextView bottomTxtAge;
    @BindView(R.id.followLay)
    RelativeLayout followLay;
    @BindView(R.id.bottomUserLay)
    LinearLayout bottomUserLay;
    @BindView(R.id.followImage)
    ImageView followImage;
    @BindView(R.id.premiumImage)
    ImageView premiumImage;
    @BindView(R.id.userLay)
    RelativeLayout userLay;
    private Context context;
    private Bundle bundle;
    private String partnerId;
    private ProfileResponse profile;
    private ApiInterface apiInterface;
    private ProfileUpdatedListener updateListener;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View controlView = inflater.inflate(R.layout.fragment_user_bottom, container, false);
        ButterKnife.bind(this, controlView);
        bundle = getArguments();
        partnerId = bundle.getString(Constants.TAG_PARTNER_ID);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        setProfileFromBundle(bundle);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bottomLay.setVisibility(View.GONE);
            }
        }, 4000);
        return controlView;
    }

    private void setProfileFromBundle(Bundle bundle) {
        profile = new ProfileResponse();
        profile.setUserId(partnerId);
        profile.setName(bundle.getString(Constants.TAG_NAME));
        profile.setUserImage(bundle.getString(Constants.TAG_USER_IMAGE));
        profile.setAge(bundle.getString(Constants.TAG_AGE));
        profile.setGender(bundle.getString(Constants.TAG_GENDER));
        profile.setDob(bundle.getString(Constants.TAG_DOB));
        profile.setPremiumMember(bundle.getString(Constants.TAG_PREMIUM_MEBER, Constants.TAG_FALSE));
        profile.setLocation(bundle.getString(Constants.TAG_LOCATION));
        profile.setFollowers(bundle.getString(Constants.TAG_FOLLOWERS));
        profile.setFollowings(bundle.getString(Constants.TAG_FOLLOWINGS));
        profile.setInterestOnYou(bundle.getBoolean(Constants.TAG_INTEREST_ON_YOU));
        profile.setInterestedByMe(bundle.getBoolean(Constants.TAG_INTERESTED_BY_ME));
        profile.setFollow(bundle.getString(Constants.TAG_FOLLOW, Constants.TAG_FALSE));
        profile.setPrivacyAge(bundle.getString(Constants.TAG_PRIVACY_AGE, Constants.TAG_FALSE));
        profile.setPrivacyContactMe(bundle.getString(Constants.TAG_PRIVACY_CONTACT_ME, Constants.TAG_FALSE));
        profile.setInterestOnYou(bundle.getBoolean(Constants.TAG_INTEREST_ON_YOU));
        profile.setInterestedByMe(bundle.getBoolean(Constants.TAG_INTERESTED_BY_ME));
        profile.setFriend(bundle.getBoolean(Constants.TAG_FRIEND));
        setProfile(profile);
    }

    private void setProfile(ProfileResponse profile) {
        if (!profile.getUserId().equals(GetSet.getUserId())) {
            bottomTxtName.setText(profile.getName());
            if (profile.getPrivacyAge().equals(Constants.TAG_FALSE)) {
                bottomTxtAge.setVisibility(View.VISIBLE);
                bottomTxtAge.setText(getString(R.string.age) + " " + profile.getAge());
            } else {
                bottomTxtAge.setText("");
                bottomTxtAge.setVisibility(View.GONE);
            }

            premiumImage.setVisibility(profile.getPremiumMember().equals(Constants.TAG_TRUE) ?
                    View.VISIBLE : View.GONE);
            Glide.with(UserBottomFragment.this)
                    .load(Constants.IMAGE_URL + profile.getUserImage())
                    .apply(App.getProfileImageRequest())
                    .into(bottomUserImage);


            followLay.setVisibility(View.GONE);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @OnClick(R.id.userLay)
    public void onUserClicked() {
        if (updateListener != null) {
            updateListener.moveToProfile();
        }
    }

    @OnClick(R.id.followLay)
    public void onViewClicked() {
        updateInterestStatus(1);
    }

    private void updateFollowStatus() {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(partnerId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (profile.getFollow().equals(Constants.TAG_TRUE)) {
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
                            profile.setFollow(Constants.TAG_TRUE);
                            App.makeToast(getString(R.string.followed_successfully));
                            setFollowStatus(profile);
                        } else {
                            profile.setFollow(Constants.TAG_FALSE);
                            App.makeToast(getString(R.string.unfollowed_successfully));
                            setFollowStatus(profile);
                        }

                        if (updateListener != null) {
                            updateListener.onProfileUpdated(partnerId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<FollowResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void updateInterestStatus(int isInterested) {
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
                            if (responseMap.get(Constants.TAG_FRIEND).equals(Constants.TAG_TRUE)) {
                                profile.setFriend(true);
                            } else {
                                profile.setFriend(false);
                            }
                        }

                        if (updateListener != null) {
                            updateListener.onProfileUpdated(partnerId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    public void setProfileUpdatedListner(ProfileUpdatedListener listener) {
        this.updateListener = listener;
    }

    public void setFollowStatus(ProfileResponse profile) {
        this.profile.setFollow(profile.getFollow());
        if (profile.getFollow().equals(Constants.TAG_FALSE)) {
            followLay.setVisibility(View.VISIBLE);
            followImage.setImageDrawable(getActivity().getDrawable(R.drawable.follow));
        } else {
            followLay.setVisibility(View.GONE);
        }
    }

}
