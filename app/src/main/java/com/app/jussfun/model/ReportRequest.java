package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class ReportRequest {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("report_by")
    private String reportBy;
    @SerializedName("report")
    private String report;
    @SerializedName("report_type")
    private String reportType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReportBy() {
        return reportBy;
    }

    public void setReportBy(String reportBy) {
        this.reportBy = reportBy;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
}

