package com.garage.management.service;

import com.garage.management.exception.InsufficientStockException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.PartModel;
import com.garage.management.model.ServicePartModel;
import com.garage.management.repository.PartRepository;
import com.garage.management.repository.FirestoreRepository;
import com.garage.management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class PartsService {

    private static final Logger log = LoggerFactory.getLogger(PartsService.class);
    private static final String SERVICE_PARTS_COLLECTION = "serviceParts";

    private final PartRepository partRepository;
    private final FirestoreRepository firestoreRepository;

    public PartsService(PartRepository partRepository, FirestoreRepository firestoreRepository) {
        this.partRepository = partRepository;
        this.firestoreRepository = firestoreRepository;
    }

    public PartModel createPart(String partName, String partNumber, double price, int stock)
            throws ExecutionException, InterruptedException {
        PartModel part = new PartModel();
        part.setPartId(partRepository.generateId());
        part.setPartName(partName);
        part.setPartNumber(partNumber);
        part.setPrice(price);
        part.setStockQuantity(stock);
        part.setActive(true);
        partRepository.save(part);
        return part;
    }

    public PartModel getPart(String partId) throws ExecutionException, InterruptedException {
        return partRepository.findById(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + partId));
    }

    public List<PartModel> getAllParts() throws ExecutionException, InterruptedException {
        return partRepository.findAll();
    }

    public List<PartModel> getActiveParts() throws ExecutionException, InterruptedException {
        return partRepository.findActive();
    }

    public PartModel updatePart(String partId, String partName, double price, int stock)
            throws ExecutionException, InterruptedException {
        PartModel part = getPart(partId);
        Map<String, Object> updates = new HashMap<>();
        if (partName != null && !partName.isBlank()) { part.setPartName(partName); updates.put("partName", partName); }
        if (price > 0) { part.setPrice(price); updates.put("price", price); }
        if (stock >= 0) { part.setStockQuantity(stock); updates.put("stockQuantity", stock); }
        partRepository.update(partId, updates);
        return part;
    }

    /**
     * Use parts for a service job. Deducts from stock (never goes negative).
     */
    public ServicePartModel usePartForJob(String serviceJobId, String partId, int quantity)
            throws ExecutionException, InterruptedException {

        PartModel part = getPart(partId);

        // Business Rule: Never allow negative stock
        if (part.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for part '" + part.getPartName() +
                    "'. Available: " + part.getStockQuantity() + ", Requested: " + quantity);
        }

        // Deduct stock
        int newStock = part.getStockQuantity() - quantity;
        partRepository.update(partId, Map.of("stockQuantity", newStock));
        part.setStockQuantity(newStock);

        // Create service part record
        ServicePartModel sp = new ServicePartModel();
        sp.setServicePartId(firestoreRepository.generateId(SERVICE_PARTS_COLLECTION));
        sp.setServiceJobId(serviceJobId);
        sp.setPartId(partId);
        sp.setPartName(part.getPartName());
        sp.setQuantity(quantity);
        sp.setUnitPrice(part.getPrice());
        sp.setSubtotal(part.getPrice() * quantity);

        firestoreRepository.save(SERVICE_PARTS_COLLECTION, sp.getServicePartId(), toMap(sp));
        log.info("Used {} x {} for service job {}. Remaining stock: {}", quantity, part.getPartName(), serviceJobId, newStock);
        return sp;
    }

    public List<ServicePartModel> getPartsForJob(String serviceJobId)
            throws ExecutionException, InterruptedException {
        return firestoreRepository.findByField(SERVICE_PARTS_COLLECTION, "serviceJobId", serviceJobId)
                .stream().map(this::spFromMap).toList();
    }

    public double calculatePartsCostForJob(String serviceJobId) throws ExecutionException, InterruptedException {
        return getPartsForJob(serviceJobId).stream()
                .mapToDouble(ServicePartModel::getSubtotal).sum();
    }

    private Map<String, Object> toMap(ServicePartModel sp) {
        Map<String, Object> data = new HashMap<>();
        data.put("servicePartId", sp.getServicePartId());
        data.put("serviceJobId", sp.getServiceJobId());
        data.put("partId", sp.getPartId());
        data.put("partName", sp.getPartName());
        data.put("quantity", sp.getQuantity());
        data.put("unitPrice", sp.getUnitPrice());
        data.put("subtotal", sp.getSubtotal());
        return data;
    }

    private ServicePartModel spFromMap(Map<String, Object> data) {
        ServicePartModel sp = new ServicePartModel();
        sp.setServicePartId((String) data.getOrDefault("servicePartId", data.get("id")));
        sp.setServiceJobId((String) data.get("serviceJobId"));
        sp.setPartId((String) data.get("partId"));
        sp.setPartName((String) data.get("partName"));
        Object qty = data.get("quantity");
        if (qty instanceof Long) sp.setQuantity(((Long) qty).intValue());
        else if (qty instanceof Integer) sp.setQuantity((Integer) qty);
        sp.setUnitPrice(toDouble(data.get("unitPrice")));
        sp.setSubtotal(toDouble(data.get("subtotal")));
        return sp;
    }

    private double toDouble(Object val) {
        if (val instanceof Double d) return d;
        if (val instanceof Long l) return l.doubleValue();
        if (val instanceof Integer i) return i.doubleValue();
        return 0.0;
    }
}
