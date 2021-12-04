package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class SubscriptionRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("membership_id")
    private String membershipId;
    @SerializedName("paid_amount")
    private String paidAmount;
    @SerializedName("transaction_id")
    private String transactionId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userName) {
        this.userId = userName;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public String getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(String paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
