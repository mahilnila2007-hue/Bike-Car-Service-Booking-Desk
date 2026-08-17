package com.garage.management.repository;

import com.garage.management.model.ServiceStatusHistoryModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class ServiceStatusHistoryRepository {

    private static final String COLLECTION = "serviceStatusHistory";
    private final FirestoreRepository repo;

    public ServiceStatusHistoryRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(ServiceStatusHistoryModel history) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, history.getHistoryId(), toMap(history));
    }

    public List<ServiceStatusHistoryModel> findByServiceJobId(String serviceJobId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "serviceJobId", serviceJobId)
                .stream().map(this::fromMap).toList();
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    private Map<String, Object> toMap(ServiceStatusHistoryModel h) {
        Map<String, Object> data = new HashMap<>();
        data.put("historyId", h.getHistoryId());
        data.put("serviceJobId", h.getServiceJobId());
        data.put("previousStatus", h.getPreviousStatus());
        data.put("newStatus", h.getNewStatus());
        data.put("changedBy", h.getChangedBy());
        if (h.getChangedAt() != null) data.put("changedAt", h.getChangedAt().toString());
        data.put("remarks", h.getRemarks());
        return data;
    }

    private ServiceStatusHistoryModel fromMap(Map<String, Object> data) {
        ServiceStatusHistoryModel h = new ServiceStatusHistoryModel();
        h.setHistoryId((String) data.getOrDefault("historyId", data.get("id")));
        h.setServiceJobId((String) data.get("serviceJobId"));
        h.setPreviousStatus((String) data.get("previousStatus"));
        h.setNewStatus((String) data.get("newStatus"));
        h.setChangedBy((String) data.get("changedBy"));
        String changedAt = (String) data.get("changedAt");
        if (changedAt != null) h.setChangedAt(java.time.Instant.parse(changedAt));
        h.setRemarks((String) data.get("remarks"));
        return h;
    }
}
