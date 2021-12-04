
package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FollowersResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("users_list")
    private List<FollowersList> followersList;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FollowersList> getFollowersList() {
        return followersList;
    }

    public void setFollowersList(List<FollowersList> followersList) {
        this.followersList = followersList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class FollowersList {

        @Expose
        private int age;
        @SerializedName("chat_notification")
        private Boolean chatNotification;
        @Expose
        private String dob;
        @Expose
        private String gender;
        @SerializedName("interest_notification")
        private Boolean interestNotification;
        @SerializedName("last_chat")
        private String lastChat;
        @SerializedName("location")
        private String location;
        @SerializedName("login_id")
        private String loginId;
        @Expose
        private String name;
        @SerializedName("premium_member")
        private String premiumMember;
        @SerializedName("privacy_age")
        private String privacyAge;
        @SerializedName("privacy_contactme")
        private String privacyContactMe;
        @SerializedName("referal_link")
        private String referalLink;
        @SerializedName("show_notification")
        private Boolean showNotification;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_image")
        private String userImage;
        @SerializedName("user_name")
        private String userName;
        @SerializedName("profile_unlocked")
        private boolean profileUnlocked;
        private boolean isPlaying;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
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

        public boolean isPlaying() {
            return isPlaying;
        }

        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }

        public Boolean getChatNotification() {
            return chatNotification;
        }

        public void setChatNotification(Boolean chatNotification) {
            this.chatNotification = chatNotification;
        }

        public Boolean getInterestNotification() {
            return interestNotification;
        }

        public void setInterestNotification(Boolean interestNotification) {
            this.interestNotification = interestNotification;
        }

        public String getLastChat() {
            return lastChat;
        }

        public void setLastChat(String lastChat) {
            this.lastChat = lastChat;
        }

        public String getReferalLink() {
            return referalLink;
        }

        public void setReferalLink(String referalLink) {
            this.referalLink = referalLink;
        }

        public Boolean getShowNotification() {
            return showNotification;
        }

        public void setShowNotification(Boolean showNotification) {
            this.showNotification = showNotification;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public boolean getProfileUnlocked() {
            return profileUnlocked;
        }

        public void setProfileUnlocked(boolean profileUnlocked) {
            this.profileUnlocked = profileUnlocked;
        }
    }

}
