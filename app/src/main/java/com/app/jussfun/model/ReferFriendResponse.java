package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class ReferFriendResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("total_gems")
    private String totalGems;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalGems() {
        return totalGems;
    }

    public void setTotalGems(String totalGems) {
        this.totalGems = totalGems;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
