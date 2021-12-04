
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String result) {
        this.message = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
