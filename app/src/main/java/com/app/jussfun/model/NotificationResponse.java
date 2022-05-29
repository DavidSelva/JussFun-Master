package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationResponse {

    @SerializedName("result")
    private List<ResultItem> result;

    @SerializedName("status")
    private String status;

    public List<ResultItem> getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public class ResultItem {

        @SerializedName("user_id")
        private String userId;

        @SerializedName("user_image")
        private String userImage;

        @SerializedName("user_name")
        private String userName;

        @SerializedName("scope")
        private String scope;

        @SerializedName("id")
        private String id;

        @SerializedName("time")
        private String time;

        @SerializedName("message")
        private String message;

        @SerializedName("feed_type")
        private String feedType;

        @SerializedName("feed_id")
        private String feedId;

        public String getUserId() {
            return userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public String getUserName() {
            return userName;
        }

        public String getScope() {
            return scope;
        }

        public String getId() {
            return id;
        }

        public String getTime() {
            return time;
        }

        public String getMessage() {
            return message;
        }

        public String getFeedType() {
            return feedType;
        }

        public String getFeedId() {
            return feedId;
        }
    }
}