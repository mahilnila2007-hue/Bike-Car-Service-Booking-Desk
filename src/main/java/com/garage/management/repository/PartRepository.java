package com.garage.management.repository;

import com.garage.management.model.PartModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class PartRepository {

    private static final String COLLECTION = "parts";
    private final FirestoreRepository repo;

    public PartRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(PartModel part) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, part.getPartId(), toMap(part));
    }

    public Optional<PartModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<PartModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<PartModel> findActive() throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "active", true).stream().map(this::fromMap).toList();
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    private Map<String, Object> toMap(PartModel p) {
        Map<String, Object> data = new HashMap<>();
        data.put("partId", p.getPartId());
        data.put("partName", p.getPartName());
        data.put("partNumber", p.getPartNumber());
        data.put("price", p.getPrice());
        data.put("stockQuantity", p.getStockQuantity());
        data.put("active", p.isActive());
        return data;
    }

    private PartModel fromMap(Map<String, Object> data) {
        PartModel p = new PartModel();
        p.setPartId((String) data.getOrDefault("partId", data.get("id")));
        p.setPartName((String) data.get("partName"));
        p.setPartNumber((String) data.get("partNumber"));
        Object price = data.get("price");
        if (price instanceof Double) p.setPrice((Double) price);
        else if (price instanceof Long) p.setPrice(((Long) price).doubleValue());
        Object stock = data.get("stockQuantity");
        if (stock instanceof Long) p.setStockQuantity(((Long) stock).intValue());
        else if (stock instanceof Integer) p.setStockQuantity((Integer) stock);
        Object active = data.get("active");
        p.setActive(active != null && (boolean) active);
        return p;
    }
}
