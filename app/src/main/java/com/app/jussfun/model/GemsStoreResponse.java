package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GemsStoreResponse {

    @SerializedName("gems_list")
    private List<GemsList> gemsList;
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public List<GemsList> getGemsList() {
        return gemsList;
    }

    public void setGemsList(List<GemsList> gemsList) {
        this.gemsList = gemsList;
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

    public class GemsList {

        @SerializedName("gem_count")
        private Double gemCount;
        @SerializedName("gem_icon")
        private String gemIcon;
        @SerializedName("gem_price")
        private String gemPrice;
        @SerializedName("gem_title")
        private String gemTitle;

        private int viewType;

        public Double getGemCount() {
            return gemCount;
        }

        public void setGemCount(Double gemCount) {
            this.gemCount = gemCount;
        }

        public String getGemIcon() {
            return gemIcon;
        }

        public void setGemIcon(String gemIcon) {
            this.gemIcon = gemIcon;
        }

        public String getGemPrice() {
            return gemPrice;
        }

        public void setGemPrice(String gemPrice) {
            this.gemPrice = gemPrice;
        }

        public String getGemTitle() {
            return gemTitle;
        }

        public void setGemTitle(String gemTitle) {
            this.gemTitle = gemTitle;
        }

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }
    }

}
