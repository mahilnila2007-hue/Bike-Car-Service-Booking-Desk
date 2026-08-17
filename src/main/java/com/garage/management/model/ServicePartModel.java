package com.garage.management.model;

/**
 * Represents a service part usage document in Firestore serviceParts/{servicePartId} collection.
 */
public class ServicePartModel {
    private String servicePartId;
    private String serviceJobId;
    private String partId;
    private String partName;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public ServicePartModel() {}

    public String getServicePartId() { return servicePartId; }
    public void setServicePartId(String servicePartId) { this.servicePartId = servicePartId; }

    public String getServiceJobId() { return serviceJobId; }
    public void setServiceJobId(String serviceJobId) { this.serviceJobId = serviceJobId; }

    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
