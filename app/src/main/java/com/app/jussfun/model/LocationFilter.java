
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;


public class LocationFilter {

    @SerializedName("name")
    private String name;
    private boolean isChecked;

    public LocationFilter(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
