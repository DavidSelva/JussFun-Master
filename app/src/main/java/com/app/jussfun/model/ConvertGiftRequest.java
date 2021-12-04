
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class ConvertGiftRequest {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("type")
    private String type;


    @SerializedName("gems_requested")
    private String gemsRequested;
    @SerializedName("paypalid")
    private String payPalId;
    @SerializedName("username")
    private String userName;

    public String getGemsRequested() {
        return gemsRequested;
    }

    public void setGemsRequested(String gemsRequested) {
        this.gemsRequested = gemsRequested;
    }

    public String getPayPalId() {
        return payPalId;
    }

    public void setPayPalId(String payPalId) {
        this.payPalId = payPalId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
