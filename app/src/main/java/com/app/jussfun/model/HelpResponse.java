
package com.app.jussfun.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HelpResponse {

    @SerializedName("help_list")
    private List<HelpList> helpList;
    @Expose
    private String status;

    public List<HelpList> getHelpList() {
        return helpList;
    }

    public void setHelpList(List<HelpList> helpList) {
        this.helpList = helpList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class HelpList {

        @SerializedName("help_descrip")
        private String helpDescription;
        @SerializedName("help_title")
        private String helpTitle;

        public String getHelpDescrip() {
            return helpDescription;
        }

        public void setHelpDescrip(String helpDescrip) {
            this.helpDescription = helpDescrip;
        }

        public String getHelpTitle() {
            return helpTitle;
        }

        public void setHelpTitle(String helpTitle) {
            this.helpTitle = helpTitle;
        }

    }

}
