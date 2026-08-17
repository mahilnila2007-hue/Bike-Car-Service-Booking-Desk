package com.garage.management.repository;

import com.garage.management.model.ServiceTypeModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class ServiceTypeRepository {

    private static final String COLLECTION = "serviceTypes";
    private final FirestoreRepository repo;

    public ServiceTypeRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(ServiceTypeModel st) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, st.getServiceTypeId(), toMap(st));
    }

    public Optional<ServiceTypeModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<ServiceTypeModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<ServiceTypeModel> findActive() throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "active", true).stream().map(this::fromMap).toList();
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

    private Map<String, Object> toMap(ServiceTypeModel st) {
        Map<String, Object> data = new HashMap<>();
        data.put("serviceTypeId", st.getServiceTypeId());
        data.put("name", st.getName());
        data.put("description", st.getDescription());
        data.put("estimatedDurationMinutes", st.getEstimatedDurationMinutes());
        data.put("defaultLabourCost", st.getDefaultLabourCost());
        data.put("active", st.isActive());
        return data;
    }

    private ServiceTypeModel fromMap(Map<String, Object> data) {
        ServiceTypeModel st = new ServiceTypeModel();
        st.setServiceTypeId((String) data.getOrDefault("serviceTypeId", data.get("id")));
        st.setName((String) data.get("name"));
        st.setDescription((String) data.get("description"));
        Object duration = data.get("estimatedDurationMinutes");
        if (duration instanceof Long) st.setEstimatedDurationMinutes(((Long) duration).intValue());
        else if (duration instanceof Integer) st.setEstimatedDurationMinutes((Integer) duration);
        Object cost = data.get("defaultLabourCost");
        if (cost instanceof Double) st.setDefaultLabourCost((Double) cost);
        else if (cost instanceof Long) st.setDefaultLabourCost(((Long) cost).doubleValue());
        Object active = data.get("active");
        st.setActive(active != null && (boolean) active);
        return st;
    }
}
