package com.app.jussfun.helper.callback;


import com.app.jussfun.model.OnlineUsers;
import com.app.jussfun.model.ProfileResponse;

public interface FollowUpdatedListener {
    void onFollowUpdated(ProfileResponse profileResponse, int holderPosition);

    void onFollowUpdated(OnlineUsers.AccountModel profileResponse, int holderPosition);

    void onProfileClicked(String userId);
}