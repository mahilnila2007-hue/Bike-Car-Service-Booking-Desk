package com.garage.management.repository;

import com.garage.management.model.ServiceJobModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class ServiceJobRepository {

    private static final String COLLECTION = "serviceJobs";
    private final FirestoreRepository repo;

    public ServiceJobRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(ServiceJobModel job) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, job.getServiceJobId(), toMap(job));
    }

    public Optional<ServiceJobModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<ServiceJobModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<ServiceJobModel> findByCustomerId(String customerId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "customerId", customerId).stream().map(this::fromMap).toList();
    }

    public List<ServiceJobModel> findByVehicleId(String vehicleId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "vehicleId", vehicleId).stream().map(this::fromMap).toList();
    }

    public List<ServiceJobModel> findByStatus(String status) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "currentStatus", status).stream().map(this::fromMap).toList();
    }

    public Optional<ServiceJobModel> findByBookingId(String bookingId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "bookingId", bookingId)
                .stream().map(this::fromMap).findFirst();
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(ServiceJobModel job) {
        Map<String, Object> data = new HashMap<>();
        data.put("serviceJobId", job.getServiceJobId());
        data.put("bookingId", job.getBookingId());
        data.put("vehicleId", job.getVehicleId());
        data.put("customerId", job.getCustomerId());
        data.put("bayId", job.getBayId());
        data.put("mechanicId", job.getMechanicId());
        if (job.getStartTime() != null) data.put("startTime", job.getStartTime().toString());
        if (job.getEstimatedCompletionTime() != null) data.put("estimatedCompletionTime", job.getEstimatedCompletionTime().toString());
        if (job.getActualCompletionTime() != null) data.put("actualCompletionTime", job.getActualCompletionTime().toString());
        data.put("currentStatus", job.getCurrentStatus());
        data.put("serviceNotes", job.getServiceNotes());
        if (job.getCreatedAt() != null) data.put("createdAt", job.getCreatedAt().toString());
        if (job.getUpdatedAt() != null) data.put("updatedAt", job.getUpdatedAt().toString());
        return data;
    }

    private ServiceJobModel fromMap(Map<String, Object> data) {
        ServiceJobModel job = new ServiceJobModel();
        job.setServiceJobId((String) data.getOrDefault("serviceJobId", data.get("id")));
        job.setBookingId((String) data.get("bookingId"));
        job.setVehicleId((String) data.get("vehicleId"));
        job.setCustomerId((String) data.get("customerId"));
        job.setBayId((String) data.get("bayId"));
        job.setMechanicId((String) data.get("mechanicId"));
        String startTime = (String) data.get("startTime");
        if (startTime != null) job.setStartTime(java.time.Instant.parse(startTime));
        String eta = (String) data.get("estimatedCompletionTime");
        if (eta != null) job.setEstimatedCompletionTime(java.time.Instant.parse(eta));
        String actual = (String) data.get("actualCompletionTime");
        if (actual != null) job.setActualCompletionTime(java.time.Instant.parse(actual));
        job.setCurrentStatus((String) data.get("currentStatus"));
        job.setServiceNotes((String) data.get("serviceNotes"));
        String createdAt = (String) data.get("createdAt");
        if (createdAt != null) job.setCreatedAt(java.time.Instant.parse(createdAt));
        String updatedAt = (String) data.get("updatedAt");
        if (updatedAt != null) job.setUpdatedAt(java.time.Instant.parse(updatedAt));
        return job;
    }
}
