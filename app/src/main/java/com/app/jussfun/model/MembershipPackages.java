package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class MembershipPackages {
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("_id")
    private String _Id;
    @SerializedName("subs_title")
    private String subsTitle;
    @SerializedName("subs_gems")
    private String subsGems;
    @SerializedName("subs_price")
    private String subsPrice;
    @SerializedName("subs_validity")
    private String subsValidity;
    @SerializedName("updated_at")
    private String updatedAt;


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String get_Id() {
        return _Id;
    }

    public void set_Id(String _Id) {
        this._Id = _Id;
    }

    public String getSubsTitle() {
        return subsTitle;
    }

    public void setSubsTitle(String subsTitle) {
        this.subsTitle = subsTitle;
    }

    public String getSubsGems() {
        return subsGems;
    }

    public void setSubsGems(String subsGems) {
        this.subsGems = subsGems;
    }

    public String getSubsPrice() {
        return subsPrice;
    }

    public void setSubsPrice(String subsPrice) {
        this.subsPrice = subsPrice;
    }

    public String getSubsValidity() {
        return subsValidity;
    }

    public void setSubsValidity(String subsValidity) {
        this.subsValidity = subsValidity;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
