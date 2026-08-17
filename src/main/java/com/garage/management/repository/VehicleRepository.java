package com.garage.management.repository;

import com.garage.management.model.VehicleModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class VehicleRepository {

    private static final String COLLECTION = "vehicles";
    private final FirestoreRepository repo;

    public VehicleRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(VehicleModel vehicle) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, vehicle.getVehicleId(), toMap(vehicle));
    }

    public Optional<VehicleModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<VehicleModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<VehicleModel> findByCustomerId(String customerId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "customerId", customerId).stream().map(this::fromMap).toList();
    }

    public Optional<VehicleModel> findByRegistrationNumber(String regNumber) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "registrationNumber", regNumber)
                .stream().map(this::fromMap).findFirst();
    }

    public boolean existsByRegistrationNumber(String regNumber) throws ExecutionException, InterruptedException {
        return repo.existsByField(COLLECTION, "registrationNumber", regNumber);
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        repo.delete(COLLECTION, id);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    private Map<String, Object> toMap(VehicleModel v) {
        Map<String, Object> data = new HashMap<>();
        data.put("vehicleId", v.getVehicleId());
        data.put("customerId", v.getCustomerId());
        data.put("registrationNumber", v.getRegistrationNumber());
        data.put("vehicleType", v.getVehicleType());
        data.put("brand", v.getBrand());
        data.put("model", v.getModel());
        data.put("manufacturingYear", v.getManufacturingYear());
        data.put("fuelType", v.getFuelType());
        data.put("mileage", v.getMileage());
        if (v.getCreatedAt() != null) data.put("createdAt", v.getCreatedAt().toString());
        if (v.getUpdatedAt() != null) data.put("updatedAt", v.getUpdatedAt().toString());
        return data;
    }

    private VehicleModel fromMap(Map<String, Object> data) {
        VehicleModel v = new VehicleModel();
        v.setVehicleId((String) data.getOrDefault("vehicleId", data.get("id")));
        v.setCustomerId((String) data.get("customerId"));
        v.setRegistrationNumber((String) data.get("registrationNumber"));
        v.setVehicleType((String) data.get("vehicleType"));
        v.setBrand((String) data.get("brand"));
        v.setModel((String) data.get("model"));
        Object year = data.get("manufacturingYear");
        if (year instanceof Long) v.setManufacturingYear(((Long) year).intValue());
        else if (year instanceof Integer) v.setManufacturingYear((Integer) year);
        v.setFuelType((String) data.get("fuelType"));
        Object mileage = data.get("mileage");
        if (mileage instanceof Long) v.setMileage(((Long) mileage).intValue());
        else if (mileage instanceof Integer) v.setMileage((Integer) mileage);
        String createdAt = (String) data.get("createdAt");
        if (createdAt != null) v.setCreatedAt(java.time.Instant.parse(createdAt));
        String updatedAt = (String) data.get("updatedAt");
        if (updatedAt != null) v.setUpdatedAt(java.time.Instant.parse(updatedAt));
        return v;
    }
}
