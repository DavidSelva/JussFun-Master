package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class GemsPurchaseResponse {

    @SerializedName("available_gems")
    private String availableGems;
    @SerializedName("status")
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("message")
    private String message;

    public String getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(String availableGems) {
        this.availableGems = availableGems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
