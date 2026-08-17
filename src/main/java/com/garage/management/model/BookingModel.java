package com.garage.management.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents a booking document in Firestore bookings/{bookingId} collection.
 */
public class BookingModel {
    private String bookingId;
    private String customerId;
    private String vehicleId;
    private String serviceType;
    private String description;
    private LocalDate bookingDate;
    private String preferredTime;
    private String status; // PENDING, CONFIRMED, VEHICLE_RECEIVED, CANCELLED, COMPLETED
    private Instant createdAt;
    private Instant updatedAt;

    public BookingModel() {}

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
