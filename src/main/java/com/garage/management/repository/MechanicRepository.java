package com.garage.management.repository;

import com.garage.management.model.MechanicModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class MechanicRepository {

    private static final String COLLECTION = "mechanics";
    private final FirestoreRepository repo;

    public MechanicRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(MechanicModel mechanic) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, mechanic.getMechanicId(), toMap(mechanic));
    }

    public Optional<MechanicModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<MechanicModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<MechanicModel> findByAvailabilityStatus(String status) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "availabilityStatus", status).stream().map(this::fromMap).toList();
    }

    public List<MechanicModel> findAvailableBySpecialization(String specialization) throws ExecutionException, InterruptedException {
        return repo.findByTwoFields(COLLECTION, "availabilityStatus", "AVAILABLE", "specialization", specialization)
                .stream().map(this::fromMap).toList();
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    private Map<String, Object> toMap(MechanicModel m) {
        Map<String, Object> data = new HashMap<>();
        data.put("mechanicId", m.getMechanicId());
        data.put("name", m.getName());
        data.put("phone", m.getPhone());
        data.put("specialization", m.getSpecialization());
        data.put("experience", m.getExperience());
        data.put("availabilityStatus", m.getAvailabilityStatus());
        return data;
    }

    private MechanicModel fromMap(Map<String, Object> data) {
        MechanicModel m = new MechanicModel();
        m.setMechanicId((String) data.getOrDefault("mechanicId", data.get("id")));
        m.setName((String) data.get("name"));
        m.setPhone((String) data.get("phone"));
        m.setSpecialization((String) data.get("specialization"));
        Object exp = data.get("experience");
        if (exp instanceof Long) m.setExperience(((Long) exp).intValue());
        else if (exp instanceof Integer) m.setExperience((Integer) exp);
        m.setAvailabilityStatus((String) data.get("availabilityStatus"));
        return m;
    }
}
