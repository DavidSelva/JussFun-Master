
package com.app.jussfun.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TermsResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("terms")
    private Terms terms;
    @SerializedName("faq")
    private List<FAQ> faqList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Terms getTerms() {
        return terms;
    }

    public void setTerms(Terms terms) {
        this.terms = terms;
    }

    public class Terms {

        @SerializedName("_id")
        private String _id;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("help_descrip")
        private String helpDescrip;
        @SerializedName("help_title")
        private String helpTitle;
        @SerializedName("updated_at")
        private String updatedAt;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getHelpDescrip() {
            return helpDescrip;
        }

        public void setHelpDescrip(String helpDescrip) {
            this.helpDescrip = helpDescrip;
        }

        public String getHelpTitle() {
            return helpTitle;
        }

        public void setHelpTitle(String helpTitle) {
            this.helpTitle = helpTitle;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

    }

    public class FAQ {
        @SerializedName("_id")
        private String _id;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("faq_descrip")
        private String faqDesc;
        @SerializedName("faq_title")
        private String faqTitle;
        @SerializedName("updated_at")
        private String updatedAt;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getFaqDesc() {
            return faqDesc;
        }

        public void setFaqDesc(String faqDesc) {
            this.faqDesc = faqDesc;
        }

        public String getFaqTitle() {
            return faqTitle;
        }

        public void setFaqTitle(String faqTitle) {
            this.faqTitle = faqTitle;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

}
