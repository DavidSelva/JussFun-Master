package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class AddDeviceRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("device_token")
    private String deviceToken;
    @SerializedName("device_type")
    private String deviceType;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("device_model")
    private String deviceModel;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
}
