package com.garage.management.model;

import java.time.Instant;

/**
 * Represents a service status history document in Firestore serviceStatusHistory/{historyId}.
 */
public class ServiceStatusHistoryModel {
    private String historyId;
    private String serviceJobId;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private Instant changedAt;
    private String remarks;

    public ServiceStatusHistoryModel() {}

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getServiceJobId() { return serviceJobId; }
    public void setServiceJobId(String serviceJobId) { this.serviceJobId = serviceJobId; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
