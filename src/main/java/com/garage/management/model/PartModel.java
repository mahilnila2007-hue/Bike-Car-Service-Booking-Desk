package com.garage.management.model;

/**
 * Represents a part document in Firestore parts/{partId} collection.
 */
public class PartModel {
    private String partId;
    private String partName;
    private String partNumber;
    private double price;
    private int stockQuantity;
    private boolean active;

    public PartModel() {}

    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
