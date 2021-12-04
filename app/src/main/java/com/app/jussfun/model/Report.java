
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class Report {

    @SerializedName("title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
