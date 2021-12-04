package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchUserResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("users_list")
    private List<ProfileResponse> userList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ProfileResponse> getUserList() {
        return userList;
    }

    public void setUserList(List<ProfileResponse> userList) {
        this.userList = userList;
    }
}
