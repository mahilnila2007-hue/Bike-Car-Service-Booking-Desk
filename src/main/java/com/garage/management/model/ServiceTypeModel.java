package com.garage.management.model;

/**
 * Represents a service type document in Firestore serviceTypes/{serviceTypeId} collection.
 */
public class ServiceTypeModel {
    private String serviceTypeId;
    private String name;
    private String description;
    private int estimatedDurationMinutes;
    private double defaultLabourCost;
    private boolean active;

    public ServiceTypeModel() {}

    public String getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(String serviceTypeId) { this.serviceTypeId = serviceTypeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public double getDefaultLabourCost() { return defaultLabourCost; }
    public void setDefaultLabourCost(double defaultLabourCost) { this.defaultLabourCost = defaultLabourCost; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
