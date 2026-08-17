package com.garage.management.repository;

import com.garage.management.model.BayModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class BayRepository {

    private static final String COLLECTION = "bays";
    private final FirestoreRepository repo;

    public BayRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(BayModel bay) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, bay.getBayId(), toMap(bay));
    }

    public Optional<BayModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<BayModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<BayModel> findByStatus(String status) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "status", status).stream().map(this::fromMap).toList();
    }

    public List<BayModel> findAvailableByType(String bayType) throws ExecutionException, InterruptedException {
        return repo.findByTwoFields(COLLECTION, "status", "AVAILABLE", "bayType", bayType)
                .stream().map(this::fromMap).toList();
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    public FirestoreRepository getRepo() { return repo; }
    public String getCollectionName() { return COLLECTION; }

    private Map<String, Object> toMap(BayModel bay) {
        Map<String, Object> data = new HashMap<>();
        data.put("bayId", bay.getBayId());
        data.put("bayNumber", bay.getBayNumber());
        data.put("bayType", bay.getBayType());
        data.put("status", bay.getStatus());
        data.put("currentServiceJobId", bay.getCurrentServiceJobId());
        return data;
    }

    private BayModel fromMap(Map<String, Object> data) {
        BayModel bay = new BayModel();
        bay.setBayId((String) data.getOrDefault("bayId", data.get("id")));
        bay.setBayNumber((String) data.get("bayNumber"));
        bay.setBayType((String) data.get("bayType"));
        bay.setStatus((String) data.get("status"));
        bay.setCurrentServiceJobId((String) data.get("currentServiceJobId"));
        return bay;
    }
}
