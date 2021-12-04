
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class ConvertGiftResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("total_gems")
    private Long totalGems;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalGems() {
        return totalGems;
    }

    public void setTotalGems(Long totalGems) {
        this.totalGems = totalGems;
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
