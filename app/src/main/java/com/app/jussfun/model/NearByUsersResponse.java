
package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class NearByUsersResponse implements Serializable {

    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("users_list")
    private List<UsersList> usersList;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public static class UsersList implements Serializable {

        @Expose
        private Long age;
        @SerializedName("chat_notification")
        private Boolean chatNotification;
        @Expose
        private String dob;
        @Expose
        private String gender;
        @SerializedName("interest_notification")
        private Boolean interestNotification;
        @SerializedName("interest_on_you")
        private String interestOnYou;
        @SerializedName("interested_by_me")
        private String interestedByMe;
        @SerializedName("last_chat")
        private String lastChat;
        @Expose
        private String location;
        @SerializedName("login_id")
        private String loginId;
        @Expose
        private String name;
        @SerializedName("premium_member")
        private String premiumMember;
        @SerializedName("privacy_age")
        private Boolean privacyAge;
        @SerializedName("privacy_contactme")
        private Boolean privacyContactme;
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

        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }

        public Boolean getChatNotification() {
            return chatNotification;
        }

        public void setChatNotification(Boolean chatNotification) {
            this.chatNotification = chatNotification;
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

        public Boolean getInterestNotification() {
            return interestNotification;
        }

        public void setInterestNotification(Boolean interestNotification) {
            this.interestNotification = interestNotification;
        }

        public String getInterestOnYou() {
            return interestOnYou;
        }

        public void setInterestOnYou(String interestOnYou) {
            this.interestOnYou = interestOnYou;
        }

        public String getInterestedByMe() {
            return interestedByMe;
        }

        public void setInterestedByMe(String interestedByMe) {
            this.interestedByMe = interestedByMe;
        }

        public String getLastChat() {
            return lastChat;
        }

        public void setLastChat(String lastChat) {
            this.lastChat = lastChat;
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

        public Boolean getPrivacyAge() {
            return privacyAge;
        }

        public void setPrivacyAge(Boolean privacyAge) {
            this.privacyAge = privacyAge;
        }

        public Boolean getPrivacyContactme() {
            return privacyContactme;
        }

        public void setPrivacyContactme(Boolean privacyContactme) {
            this.privacyContactme = privacyContactme;
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

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

    }

}
