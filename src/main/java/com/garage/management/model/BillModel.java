package com.garage.management.model;

import java.time.Instant;

/**
 * Represents a bill document in Firestore bills/{billId} collection.
 */
public class BillModel {
    private String billId;
    private String bookingId;
    private String serviceJobId;
    private String customerId;
    private double labourCost;
    private double partsCost;
    private double taxPercentage;
    private double taxAmount;
    private double discount;
    private double totalAmount;
    private String paymentStatus; // PENDING, PAID
    private Instant generatedAt;
    private Instant paidAt;

    public BillModel() {}

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getServiceJobId() { return serviceJobId; }
    public void setServiceJobId(String serviceJobId) { this.serviceJobId = serviceJobId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public double getLabourCost() { return labourCost; }
    public void setLabourCost(double labourCost) { this.labourCost = labourCost; }

    public double getPartsCost() { return partsCost; }
    public void setPartsCost(double partsCost) { this.partsCost = partsCost; }

    public double getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(double taxPercentage) { this.taxPercentage = taxPercentage; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
