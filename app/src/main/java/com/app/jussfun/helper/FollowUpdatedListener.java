package com.app.jussfun.helper;


import com.app.jussfun.model.ProfileResponse;

public interface FollowUpdatedListener {
        void onFollowUpdated(ProfileResponse profileResponse);
    }