
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class GiftsDetails {

    @SerializedName("gift_amount")
    private String giftAmount;
    @SerializedName("gift_condition")
    private String giftCondition;
    @SerializedName("gift_equal_to")
    private String giftEqualTo;

    public String getGiftAmount() {
        return giftAmount;
    }

    public void setGiftAmount(String giftAmount) {
        this.giftAmount = giftAmount;
    }

    public String getGiftCondition() {
        return giftCondition;
    }

    public void setGiftCondition(String giftCondition) {
        this.giftCondition = giftCondition;
    }

    public String getGiftEqualTo() {
        return giftEqualTo;
    }

    public void setGiftEqualTo(String giftEqualTo) {
        this.giftEqualTo = giftEqualTo;
    }

}
