
package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecentHistoryResponse {

    @Expose
    private String status;
    @SerializedName("users_list")
    private List<UsersList> usersList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<UsersList> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<UsersList> usersList) {
        this.usersList = usersList;
    }

    public class UsersList {

        @SerializedName("age")
        private Long age;
        @SerializedName("dob")
        private String dob;
        @SerializedName("follow")
        private String follow;
        @SerializedName("gender")
        private String gender;
        @SerializedName("location")
        private String location;
        @SerializedName("login_id")
        private String loginId;
        @SerializedName("name")
        private String name;
        @SerializedName("premium_member")
        private String premiumMember;
        @SerializedName("privacy_age")
        private String privacyAge;
        @SerializedName("privacy_contactme")
        private String privacyContactMe;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_image")
        private String userImage;
        @SerializedName("last_chat")
        private String lastChat;

        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getFollow() {
            return follow;
        }

        public void setFollow(String follow) {
            this.follow = follow;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLoginId() {
            return loginId;
        }

        public void setLoginId(String loginId) {
            this.loginId = loginId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPremiumMember() {
            return premiumMember;
        }

        public void setPremiumMember(String premiumMember) {
            this.premiumMember = premiumMember;
        }

        public String getPrivacyAge() {
            return privacyAge;
        }

        public void setPrivacyAge(String privacyAge) {
            this.privacyAge = privacyAge;
        }

        public String getPrivacyContactMe() {
            return privacyContactMe;
        }

        public void setPrivacyContactMe(String privacyContactMe) {
            this.privacyContactMe = privacyContactMe;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getLastChat() {
            return lastChat;
        }

        public void setLastChat(String lastChat) {
            this.lastChat = lastChat;
        }
    }

}
