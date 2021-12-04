package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class FollowRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("follower_id")
    private String followerId;
    @SerializedName("type")
    private String type;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
