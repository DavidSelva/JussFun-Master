
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class ReportResponse {

    @SerializedName("message")
    private String message;
    @SerializedName("report_id")
    private String reportId;
    @SerializedName("status")
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
