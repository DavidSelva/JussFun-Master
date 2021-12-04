
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class Gift {

    @SerializedName("gft_icon")
    private String giftIcon;
    @SerializedName("_id")
    private String giftId;
    @SerializedName("gft_gems")
    private int giftGems;
    @SerializedName("gft_gems_prime")
    private int giftGemsPrime;
    @SerializedName("gft_title")
    private String giftTitle;

    /*Non API keys used for Socket*/
    private String userId;
    private String partnerId;

    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public int getGiftGems() {
        return giftGems;
    }

    public void setGiftGems(int giftGems) {
        this.giftGems = giftGems;
    }

    public int getGiftGemsPrime() {
        return giftGemsPrime;
    }

    public void setGiftGemsPrime(int giftGemsPrime) {
        this.giftGemsPrime = giftGemsPrime;
    }

    public String getGiftTitle() {
        return giftTitle;
    }

    public void setGiftTitle(String giftTitle) {
        this.giftTitle = giftTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
}
