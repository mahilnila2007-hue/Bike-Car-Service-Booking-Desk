package com.garage.management.repository;

import com.garage.management.model.BillModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class BillRepository {

    private static final String COLLECTION = "bills";
    private final FirestoreRepository repo;

    public BillRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(BillModel bill) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, bill.getBillId(), toMap(bill));
    }

    public Optional<BillModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<BillModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<BillModel> findByCustomerId(String customerId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "customerId", customerId).stream().map(this::fromMap).toList();
    }

    public Optional<BillModel> findByServiceJobId(String serviceJobId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "serviceJobId", serviceJobId)
                .stream().map(this::fromMap).findFirst();
    }

    public List<BillModel> findByPaymentStatus(String status) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "paymentStatus", status).stream().map(this::fromMap).toList();
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    private Map<String, Object> toMap(BillModel b) {
        Map<String, Object> data = new HashMap<>();
        data.put("billId", b.getBillId());
        data.put("bookingId", b.getBookingId());
        data.put("serviceJobId", b.getServiceJobId());
        data.put("customerId", b.getCustomerId());
        data.put("labourCost", b.getLabourCost());
        data.put("partsCost", b.getPartsCost());
        data.put("taxPercentage", b.getTaxPercentage());
        data.put("taxAmount", b.getTaxAmount());
        data.put("discount", b.getDiscount());
        data.put("totalAmount", b.getTotalAmount());
        data.put("paymentStatus", b.getPaymentStatus());
        if (b.getGeneratedAt() != null) data.put("generatedAt", b.getGeneratedAt().toString());
        if (b.getPaidAt() != null) data.put("paidAt", b.getPaidAt().toString());
        return data;
    }

    private BillModel fromMap(Map<String, Object> data) {
        BillModel b = new BillModel();
        b.setBillId((String) data.getOrDefault("billId", data.get("id")));
        b.setBookingId((String) data.get("bookingId"));
        b.setServiceJobId((String) data.get("serviceJobId"));
        b.setCustomerId((String) data.get("customerId"));
        b.setLabourCost(toDouble(data.get("labourCost")));
        b.setPartsCost(toDouble(data.get("partsCost")));
        b.setTaxPercentage(toDouble(data.get("taxPercentage")));
        b.setTaxAmount(toDouble(data.get("taxAmount")));
        b.setDiscount(toDouble(data.get("discount")));
        b.setTotalAmount(toDouble(data.get("totalAmount")));
        b.setPaymentStatus((String) data.get("paymentStatus"));
        String generatedAt = (String) data.get("generatedAt");
        if (generatedAt != null) b.setGeneratedAt(java.time.Instant.parse(generatedAt));
        String paidAt = (String) data.get("paidAt");
        if (paidAt != null) b.setPaidAt(java.time.Instant.parse(paidAt));
        return b;
    }

    private double toDouble(Object val) {
        if (val instanceof Double d) return d;
        if (val instanceof Long l) return l.doubleValue();
        if (val instanceof Integer i) return i.doubleValue();
        return 0.0;
    }
}
