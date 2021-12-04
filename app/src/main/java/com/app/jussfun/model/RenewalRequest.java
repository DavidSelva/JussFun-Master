package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class RenewalRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("package_id")
    private String packageId;
    @SerializedName("renewal_time")
    private String renewalTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getRenewalTime() {
        return renewalTime;
    }

    public void setRenewalTime(String renewalTime) {
        this.renewalTime = renewalTime;
    }
}
