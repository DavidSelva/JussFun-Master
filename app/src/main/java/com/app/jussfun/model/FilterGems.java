package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class FilterGems {
    @SerializedName("sub")
    private String subFilterPrice = "0";
    @SerializedName("unsub")
    private String unSubFilterPrice = "0";

    public String getSubFilterPrice() {
        return subFilterPrice;
    }

    public void setSubFilterPrice(String subFilterPrice) {
        this.subFilterPrice = subFilterPrice;
    }

    public String getUnSubFilterPrice() {
        return unSubFilterPrice;
    }

    public void setUnSubFilterPrice(String unSubFilterPrice) {
        this.unSubFilterPrice = unSubFilterPrice;
    }
}
