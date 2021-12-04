
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class SubscriptionResponse {

    @SerializedName("available_gems")
    private String availableGems;
    @SerializedName("premium_expiry_date")
    private String premiumExpiryDate;
    @SerializedName("premium_member")
    private String premiumMember;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public String getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(String availableGems) {
        this.availableGems = availableGems;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
