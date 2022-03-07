package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class  LikedUsersModel {
    @SerializedName("status")
    private String status;
    @SerializedName("result")
    private List<ProfileResponse> usersList;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ProfileResponse> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<ProfileResponse> usersList) {
        this.usersList = usersList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
