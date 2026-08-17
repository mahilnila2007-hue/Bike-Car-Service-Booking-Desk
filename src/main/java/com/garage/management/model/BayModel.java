package com.garage.management.model;

/**
 * Represents a service bay document in Firestore bays/{bayId} collection.
 */
public class BayModel {
    private String bayId;
    private String bayNumber;
    private String bayType; // TWO_WHEELER, FOUR_WHEELER, UNIVERSAL
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE
    private String currentServiceJobId;

    public BayModel() {}

    public String getBayId() { return bayId; }
    public void setBayId(String bayId) { this.bayId = bayId; }

    public String getBayNumber() { return bayNumber; }
    public void setBayNumber(String bayNumber) { this.bayNumber = bayNumber; }

    public String getBayType() { return bayType; }
    public void setBayType(String bayType) { this.bayType = bayType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentServiceJobId() { return currentServiceJobId; }
    public void setCurrentServiceJobId(String currentServiceJobId) { this.currentServiceJobId = currentServiceJobId; }
}
