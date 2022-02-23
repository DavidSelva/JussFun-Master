package com.app.jussfun.helper.callback;


import com.app.jussfun.model.ProfileResponse;

public interface FollowUpdatedListener {
        void onFollowUpdated(ProfileResponse profileResponse);
    }