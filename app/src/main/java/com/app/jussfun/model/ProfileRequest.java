package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class ProfileRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("name")
    private String name;
    @SerializedName("call_type")
    private String callType;
    @SerializedName("paypal_id")
    private String payPalId;
    @SerializedName("profile_id")
    private String profileId;
    @SerializedName("privacy_age")
    private String privacyAge;
    @SerializedName("privacy_contactme")
    private String privacyContactMe;
    @SerializedName("show_notification")
    private String showNotification;
    @SerializedName("chat_notification")
    private String chatNotification;
    @SerializedName("follow_notification")
    private String followNotification;
    @SerializedName("interest_notification")
    private String interestNotification;
    @SerializedName("location")
    private String location;
    @SerializedName("age")
    private String age;
    @SerializedName("dob")
    private String dob;
    @SerializedName("bank_accountno")
    private String bankAccNo;
    @SerializedName("bank_ifsccode")
    private String bankIfscCode;
    @SerializedName("bank_accountname")
    private String bankAccName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayPalId() {
        return payPalId;
    }

    public void setPayPalId(String payPalId) {
        this.payPalId = payPalId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
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

    public String getShowNotification() {
        return showNotification;
    }

    public void setShowNotification(String showNotification) {
        this.showNotification = showNotification;
    }

    public String getChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(String chatNotification) {
        this.chatNotification = chatNotification;
    }

    public String getFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(String followNotification) {
        this.followNotification = followNotification;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInterestNotification() {
        return interestNotification;
    }

    public void setInterestNotification(String interestNotification) {
        this.interestNotification = interestNotification;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getBankAccNo() {
        return bankAccNo;
    }

    public void setBankAccNo(String bankAccNo) {
        this.bankAccNo = bankAccNo;
    }

    public String getBankIfscCode() {
        return bankIfscCode;
    }

    public void setBankIfscCode(String bankIfscCode) {
        this.bankIfscCode = bankIfscCode;
    }

    public String getBankAccName() {
        return bankAccName;
    }

    public void setBankAccName(String bankAccName) {
        this.bankAccName = bankAccName;
    }
}
