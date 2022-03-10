package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OnlineUsers {

    @SerializedName("result")
    private List<AccountModel> result;

    @SerializedName("status")
    private String status;

    public List<AccountModel> getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public class AccountModel {

        @SerializedName("acct_call_preference")
        private String acctCallPreference;

        @SerializedName("acct_show_contactme")
        private boolean acctShowContactme;

        @SerializedName("acct_membership_till")
        private String acctMembershipTill;

        @SerializedName("acct_last_active")
        private String acctLastActive;

        @SerializedName("acct_age")
        private int acctAge;

        @SerializedName("acct_membership")
        private String acctMembership;

        @SerializedName("acct_chat_alert")
        private boolean acctChatAlert;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("acct_status")
        private int acctStatus;

        @SerializedName("__v")
        private int V;

        @SerializedName("acct_gems")
        private int acctGems;

        @SerializedName("acct_birthday")
        private String acctBirthday;

        @SerializedName("acct_facebookid")
        private String acctFacebookid;

        @SerializedName("acct_createdat")
        private String acctCreatedat;

        @SerializedName("acct_name")
        private String acctName;

        @SerializedName("acct_username")
        private String acctUsername;

        @SerializedName("acct_show_age")
        private boolean acctShowAge;

        @SerializedName("acct_follow_alert")
        private boolean acctFollowAlert;

        @SerializedName("acct_location")
        private String acctLocation;

        @SerializedName("acct_alert")
        private boolean acctAlert;

        @SerializedName("acct_sync")
        private String acctSync;

        @SerializedName("acct_followings_count")
        private int acctFollowingsCount;

        @SerializedName("acct_platform")
        private String acctPlatform;

        @SerializedName("acct_gender")
        private String acctGender;

        @SerializedName("acct_photo")
        private String acctPhoto;

        @SerializedName("acct_referral_code")
        private String acctReferralCode;

        @SerializedName("acct_live")
        private String acctLive;

        @SerializedName("acct_interest_alert")
        private boolean acctInterestAlert;

        @SerializedName("acct_free_unlocks_left")
        private int acctFreeUnlocksLeft;

        @SerializedName("acct_followers_count")
        private int acctFollowersCount;

        @SerializedName("acct_mailid")
        private String acctMailid;

        @SerializedName("acct_gift_earnings")
        private int acctGiftEarnings;

        @SerializedName("_id")
        private String id;

        @SerializedName("acct_gifts")
        private int acctGifts;

        public String getAcctCallPreference() {
            return acctCallPreference;
        }

        public boolean isAcctShowContactme() {
            return acctShowContactme;
        }

        public String getAcctMembershipTill() {
            return acctMembershipTill;
        }

        public String getAcctLastActive() {
            return acctLastActive;
        }

        public int getAcctAge() {
            return acctAge;
        }

        public String getAcctMembership() {
            return acctMembership;
        }

        public boolean isAcctChatAlert() {
            return acctChatAlert;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public int getAcctStatus() {
            return acctStatus;
        }

        public int getV() {
            return V;
        }

        public int getAcctGems() {
            return acctGems;
        }

        public String getAcctBirthday() {
            return acctBirthday;
        }

        public String getAcctFacebookid() {
            return acctFacebookid;
        }

        public String getAcctCreatedat() {
            return acctCreatedat;
        }

        public String getAcctName() {
            return acctName;
        }

        public String getAcctUsername() {
            return acctUsername;
        }

        public boolean isAcctShowAge() {
            return acctShowAge;
        }

        public boolean isAcctFollowAlert() {
            return acctFollowAlert;
        }

        public String getAcctLocation() {
            return acctLocation;
        }

        public boolean isAcctAlert() {
            return acctAlert;
        }

        public String getAcctSync() {
            return acctSync;
        }

        public int getAcctFollowingsCount() {
            return acctFollowingsCount;
        }

        public String getAcctPlatform() {
            return acctPlatform;
        }

        public String getAcctGender() {
            return acctGender;
        }

        public String getAcctPhoto() {
            return acctPhoto;
        }

        public String getAcctReferralCode() {
            return acctReferralCode;
        }

        public String getAcctLive() {
            return acctLive;
        }

        public boolean isAcctInterestAlert() {
            return acctInterestAlert;
        }

        public int getAcctFreeUnlocksLeft() {
            return acctFreeUnlocksLeft;
        }

        public int getAcctFollowersCount() {
            return acctFollowersCount;
        }

        public String getAcctMailid() {
            return acctMailid;
        }

        public int getAcctGiftEarnings() {
            return acctGiftEarnings;
        }

        public String getId() {
            return id;
        }

        public int getAcctGifts() {
            return acctGifts;
        }

        public void setAcctCallPreference(String acctCallPreference) {
            this.acctCallPreference = acctCallPreference;
        }

        public void setAcctShowContactme(boolean acctShowContactme) {
            this.acctShowContactme = acctShowContactme;
        }

        public void setAcctMembershipTill(String acctMembershipTill) {
            this.acctMembershipTill = acctMembershipTill;
        }

        public void setAcctLastActive(String acctLastActive) {
            this.acctLastActive = acctLastActive;
        }

        public void setAcctAge(int acctAge) {
            this.acctAge = acctAge;
        }

        public void setAcctMembership(String acctMembership) {
            this.acctMembership = acctMembership;
        }

        public void setAcctChatAlert(boolean acctChatAlert) {
            this.acctChatAlert = acctChatAlert;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public void setAcctStatus(int acctStatus) {
            this.acctStatus = acctStatus;
        }

        public void setV(int v) {
            V = v;
        }

        public void setAcctGems(int acctGems) {
            this.acctGems = acctGems;
        }

        public void setAcctBirthday(String acctBirthday) {
            this.acctBirthday = acctBirthday;
        }

        public void setAcctFacebookid(String acctFacebookid) {
            this.acctFacebookid = acctFacebookid;
        }

        public void setAcctCreatedat(String acctCreatedat) {
            this.acctCreatedat = acctCreatedat;
        }

        public void setAcctName(String acctName) {
            this.acctName = acctName;
        }

        public void setAcctUsername(String acctUsername) {
            this.acctUsername = acctUsername;
        }

        public void setAcctShowAge(boolean acctShowAge) {
            this.acctShowAge = acctShowAge;
        }

        public void setAcctFollowAlert(boolean acctFollowAlert) {
            this.acctFollowAlert = acctFollowAlert;
        }

        public void setAcctLocation(String acctLocation) {
            this.acctLocation = acctLocation;
        }

        public void setAcctAlert(boolean acctAlert) {
            this.acctAlert = acctAlert;
        }

        public void setAcctSync(String acctSync) {
            this.acctSync = acctSync;
        }

        public void setAcctFollowingsCount(int acctFollowingsCount) {
            this.acctFollowingsCount = acctFollowingsCount;
        }

        public void setAcctPlatform(String acctPlatform) {
            this.acctPlatform = acctPlatform;
        }

        public void setAcctGender(String acctGender) {
            this.acctGender = acctGender;
        }

        public void setAcctPhoto(String acctPhoto) {
            this.acctPhoto = acctPhoto;
        }

        public void setAcctReferralCode(String acctReferralCode) {
            this.acctReferralCode = acctReferralCode;
        }

        public void setAcctLive(String acctLive) {
            this.acctLive = acctLive;
        }

        public void setAcctInterestAlert(boolean acctInterestAlert) {
            this.acctInterestAlert = acctInterestAlert;
        }

        public void setAcctFreeUnlocksLeft(int acctFreeUnlocksLeft) {
            this.acctFreeUnlocksLeft = acctFreeUnlocksLeft;
        }

        public void setAcctFollowersCount(int acctFollowersCount) {
            this.acctFollowersCount = acctFollowersCount;
        }

        public void setAcctMailid(String acctMailid) {
            this.acctMailid = acctMailid;
        }

        public void setAcctGiftEarnings(int acctGiftEarnings) {
            this.acctGiftEarnings = acctGiftEarnings;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setAcctGifts(int acctGifts) {
            this.acctGifts = acctGifts;
        }
    }
}