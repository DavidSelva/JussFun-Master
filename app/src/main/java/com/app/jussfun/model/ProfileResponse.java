
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileResponse implements Serializable {

    private String age;
    @SerializedName("available_gems")
    private String availableGems;
    @SerializedName("available_gifts")
    private String availableGifts;
    @SerializedName("dob")
    private String dob;
    @SerializedName("followers")
    private String followers;
    @SerializedName("followings")
    private String followings;
    @SerializedName("gender")
    private String gender;
    @SerializedName("location")
    private String location;
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("name")
    private String name;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("paypal_id")
    private String paypalId;
    @SerializedName("premium_expiry_date")
    private String premiumExpiryDate;
    @SerializedName("premium_member")
    private String premiumMember;
    @SerializedName("referal_link")
    private String referalLink;
    @SerializedName("status")
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_image")
    private String userImage;
    @SerializedName("message")
    private String message;
    @SerializedName("follow")
    private String follow;
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
    @SerializedName("gift_earnings")
    private String giftEarnings;
    @SerializedName("feed_count")
    private String feedCount;
    @SerializedName("report_user")
    private boolean reportUser;

    @SerializedName("gift_coversion_earnings")
    private String giftCoversionEarnings;
    @SerializedName("gift_conversion_value")
    private String giftConversionValue;

    @SerializedName("blocked_by_me")
    private String blockedByMe;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("videos_count")
    private String videosCount;
    @SerializedName("interest_notification")
    private boolean interestNotification;
    @SerializedName("interest_on_you")
    private boolean interestOnYou;
    @SerializedName("interested_by_me")
    private boolean interestedByMe;
    @SerializedName("interests")
    private int interests;
    @SerializedName("friends")
    private int friends;
    @SerializedName("unlocks_left")
    private int unlocksLeft;
    @SerializedName("friend")
    private boolean isFriend;
    @SerializedName("declined")
    private boolean declined;
    @SerializedName("bank_accountno")
    private String bankAccNo;
    @SerializedName("bank_ifsccode")
    private String bankIfscCode;
    @SerializedName("bank_accountname")
    private String bankAccName;

    private String app_name;

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getGiftCoversionEarnings() {
        return giftCoversionEarnings;
    }

    public void setGiftCoversionEarnings(String giftCoversionEarnings) {
        this.giftCoversionEarnings = giftCoversionEarnings;
    }

    public String getGiftConversionValue() {
        return giftConversionValue;
    }

    public void setGiftConversionValue(String giftConversionValue) {
        this.giftConversionValue = giftConversionValue;
    }



    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(String availableGems) {
        this.availableGems = availableGems;
    }

    public String getAvailableGifts() {
        return availableGifts;
    }

    public void setAvailableGifts(String availableGifts) {
        this.availableGifts = availableGifts;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getFollowings() {
        return followings;
    }

    public void setFollowings(String followings) {
        this.followings = followings;
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

    public String getPaypalId() {
        return paypalId;
    }

    public void setPaypalId(String paypalId) {
        this.paypalId = paypalId;
    }

    public String getPremiumExpiryDate() {
        return premiumExpiryDate;
    }

    public void setPremiumExpiryDate(String premiumExpiryDate) {
        this.premiumExpiryDate = premiumExpiryDate;
    }

    public String getPremiumMember() {
        return premiumMember;
    }

    public void setPremiumMember(String premiumMember) {
        this.premiumMember = premiumMember;
    }

    public String getReferalLink() {
        return referalLink;
    }

    public void setReferalLink(String referalLink) {
        this.referalLink = referalLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
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

    public boolean isReportUser() {
        return reportUser;
    }

    public void setReportUser(boolean reportUser) {
        this.reportUser = reportUser;
    }

    public String getGiftEarnings() {
        return giftEarnings;
    }

    public void setGiftEarnings(String giftEarnings) {
        this.giftEarnings = giftEarnings;
    }

    public String getBlockedByMe() {
        return blockedByMe;
    }

    public void setBlockedByMe(String blockedByMe) {
        this.blockedByMe = blockedByMe;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(String videosCount) {
        this.videosCount = videosCount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean getInterestNotification() {
        return interestNotification;
    }

    public void setInterestNotification(boolean interestNotification) {
        this.interestNotification = interestNotification;
    }

    public boolean getInterestOnYou() {
        return interestOnYou;
    }

    public void setInterestOnYou(boolean interestOnYou) {
        this.interestOnYou = interestOnYou;
    }

    public boolean getInterestedByMe() {
        return interestedByMe;
    }

    public void setInterestedByMe(boolean interestedByMe) {
        this.interestedByMe = interestedByMe;
    }

    public int getInterests() {
        return interests;
    }

    public void setInterests(int interests) {
        this.interests = interests;
    }

    public int getFriends() {
        return friends;
    }

    public void setFriends(int friends) {
        this.friends = friends;
    }

    public boolean getIsFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public int getUnlocksLeft() {
        return unlocksLeft;
    }

    public void setUnlocksLeft(int unlocksLeft) {
        this.unlocksLeft = unlocksLeft;
    }

    public boolean getDeclined() {
        return declined;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    public String getFeedCount() {
        return feedCount;
    }

    public void setFeedCount(String feedCount) {
        this.feedCount = feedCount;
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
