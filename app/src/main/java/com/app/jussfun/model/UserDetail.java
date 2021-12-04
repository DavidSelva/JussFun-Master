
package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetail {

    @SerializedName("__v")
    private Long _V;
    @Expose
    private String _id;
    @SerializedName("acct_age")
    private Long acctAge;
    @SerializedName("acct_birthday")
    private String acctBirthday;
    @SerializedName("acct_createdat")
    private String acctCreatedat;
    @SerializedName("acct_filter_on")
    private String acctFilterOn;
    @SerializedName("acct_gems")
    private Long acctGems;
    @SerializedName("acct_gender")
    private String acctGender;
    @SerializedName("acct_gift_earnings")
    private Long acctGiftEarnings;
    @SerializedName("acct_gifts")
    private Long acctGifts;
    @SerializedName("acct_live")
    private String acctLive;
    @SerializedName("acct_location")
    private String acctLocation;
    @SerializedName("acct_membership")
    private String acctMembership;
    @SerializedName("acct_membership_till")
    private String acctMembershipTill;
    @SerializedName("acct_name")
    private String acctName;
    @SerializedName("acct_payment_id")
    private String acctPaymentId;
    @SerializedName("acct_phoneno")
    private Long acctPhoneno;
    @SerializedName("acct_photo")
    private String acctPhoto;
    @SerializedName("acct_status")
    private Long acctStatus;
    @SerializedName("acct_sync")
    private String acctSync;
    @SerializedName("updated_at")
    private String updatedAt;

    public Long get_V() {
        return _V;
    }

    public void set_V(Long _V) {
        this._V = _V;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Long getAcctAge() {
        return acctAge;
    }

    public void setAcctAge(Long acctAge) {
        this.acctAge = acctAge;
    }

    public String getAcctBirthday() {
        return acctBirthday;
    }

    public void setAcctBirthday(String acctBirthday) {
        this.acctBirthday = acctBirthday;
    }

    public String getAcctCreatedat() {
        return acctCreatedat;
    }

    public void setAcctCreatedat(String acctCreatedat) {
        this.acctCreatedat = acctCreatedat;
    }

    public String getAcctFilterOn() {
        return acctFilterOn;
    }

    public void setAcctFilterOn(String acctFilterOn) {
        this.acctFilterOn = acctFilterOn;
    }

    public Long getAcctGems() {
        return acctGems;
    }

    public void setAcctGems(Long acctGems) {
        this.acctGems = acctGems;
    }

    public String getAcctGender() {
        return acctGender;
    }

    public void setAcctGender(String acctGender) {
        this.acctGender = acctGender;
    }

    public Long getAcctGiftEarnings() {
        return acctGiftEarnings;
    }

    public void setAcctGiftEarnings(Long acctGiftEarnings) {
        this.acctGiftEarnings = acctGiftEarnings;
    }

    public Long getAcctGifts() {
        return acctGifts;
    }

    public void setAcctGifts(Long acctGifts) {
        this.acctGifts = acctGifts;
    }

    public String getAcctLive() {
        return acctLive;
    }

    public void setAcctLive(String acctLive) {
        this.acctLive = acctLive;
    }

    public String getAcctLocation() {
        return acctLocation;
    }

    public void setAcctLocation(String acctLocation) {
        this.acctLocation = acctLocation;
    }

    public String getAcctMembership() {
        return acctMembership;
    }

    public void setAcctMembership(String acctMembership) {
        this.acctMembership = acctMembership;
    }

    public String getAcctMembershipTill() {
        return acctMembershipTill;
    }

    public void setAcctMembershipTill(String acctMembershipTill) {
        this.acctMembershipTill = acctMembershipTill;
    }

    public String getAcctName() {
        return acctName;
    }

    public void setAcctName(String acctName) {
        this.acctName = acctName;
    }

    public String getAcctPaymentId() {
        return acctPaymentId;
    }

    public void setAcctPaymentId(String acctPaymentId) {
        this.acctPaymentId = acctPaymentId;
    }

    public Long getAcctPhoneno() {
        return acctPhoneno;
    }

    public void setAcctPhoneno(Long acctPhoneno) {
        this.acctPhoneno = acctPhoneno;
    }

    public String getAcctPhoto() {
        return acctPhoto;
    }

    public void setAcctPhoto(String acctPhoto) {
        this.acctPhoto = acctPhoto;
    }

    public Long getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(Long acctStatus) {
        this.acctStatus = acctStatus;
    }

    public String getAcctSync() {
        return acctSync;
    }

    public void setAcctSync(String acctSync) {
        this.acctSync = acctSync;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
