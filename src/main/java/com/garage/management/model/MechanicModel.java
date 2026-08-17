package com.garage.management.model;

/**
 * Represents a mechanic document in Firestore mechanics/{mechanicId} collection.
 */
public class MechanicModel {
    private String mechanicId;
    private String name;
    private String phone;
    private String specialization; // TWO_WHEELER, FOUR_WHEELER, ENGINE, ELECTRICAL, GENERAL
    private int experience;
    private String availabilityStatus; // AVAILABLE, BUSY, ON_LEAVE

    public MechanicModel() {}

    public String getMechanicId() { return mechanicId; }
    public void setMechanicId(String mechanicId) { this.mechanicId = mechanicId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
}
