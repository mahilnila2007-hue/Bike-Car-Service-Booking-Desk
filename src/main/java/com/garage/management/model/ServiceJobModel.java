package com.garage.management.model;

import java.time.Instant;
import java.util.List;

/**
 * Represents a service job document in Firestore serviceJobs/{serviceJobId} collection.
 */
public class ServiceJobModel {
    private String serviceJobId;
    private String bookingId;
    private String vehicleId;
    private String customerId;
    private String bayId;
    private String mechanicId;
    private Instant startTime;
    private Instant estimatedCompletionTime;
    private Instant actualCompletionTime;
    private String currentStatus;
    private String serviceNotes;
    private List<String> statusHistory;
    private Instant createdAt;
    private Instant updatedAt;

    public ServiceJobModel() {}

    public String getServiceJobId() { return serviceJobId; }
    public void setServiceJobId(String serviceJobId) { this.serviceJobId = serviceJobId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getBayId() { return bayId; }
    public void setBayId(String bayId) { this.bayId = bayId; }

    public String getMechanicId() { return mechanicId; }
    public void setMechanicId(String mechanicId) { this.mechanicId = mechanicId; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(Instant estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }

    public Instant getActualCompletionTime() { return actualCompletionTime; }
    public void setActualCompletionTime(Instant actualCompletionTime) { this.actualCompletionTime = actualCompletionTime; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getServiceNotes() { return serviceNotes; }
    public void setServiceNotes(String serviceNotes) { this.serviceNotes = serviceNotes; }

    public List<String> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<String> statusHistory) { this.statusHistory = statusHistory; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
