package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class FollowResponse {
    @SerializedName("result")
    private String result;
    @SerializedName("status")
    private String status;
    @SerializedName("follow")
    private String follow;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }
}
