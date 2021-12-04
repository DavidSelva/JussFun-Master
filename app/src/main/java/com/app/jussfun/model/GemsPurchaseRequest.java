package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class GemsPurchaseRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("gem_id")
    private String gemId;
    @SerializedName("paid_amount")
    private String paidAmount;
    @SerializedName("transaction_id")
    private String transactionId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGemId() {
        return gemId;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
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
