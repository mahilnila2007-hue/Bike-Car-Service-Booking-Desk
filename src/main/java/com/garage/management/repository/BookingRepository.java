package com.garage.management.repository;

import com.garage.management.model.BookingModel;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class BookingRepository {

    private static final String COLLECTION = "bookings";
    private final FirestoreRepository repo;

    public BookingRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(BookingModel booking) throws ExecutionException, InterruptedException {
        repo.save(COLLECTION, booking.getBookingId(), toMap(booking));
    }

    public Optional<BookingModel> findById(String id) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, id).map(this::fromMap);
    }

    public List<BookingModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<BookingModel> findByCustomerId(String customerId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "customerId", customerId).stream().map(this::fromMap).toList();
    }

    public List<BookingModel> findByVehicleId(String vehicleId) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "vehicleId", vehicleId).stream().map(this::fromMap).toList();
    }

    public List<BookingModel> findByStatus(String status) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "status", status).stream().map(this::fromMap).toList();
    }

    public void update(String id, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, id, fields);
    }

    public String generateId() {
        return repo.generateId(COLLECTION);
    }

    private Map<String, Object> toMap(BookingModel b) {
        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", b.getBookingId());
        data.put("customerId", b.getCustomerId());
        data.put("vehicleId", b.getVehicleId());
        data.put("serviceType", b.getServiceType());
        data.put("description", b.getDescription());
        if (b.getBookingDate() != null) data.put("bookingDate", b.getBookingDate().toString());
        data.put("preferredTime", b.getPreferredTime());
        data.put("status", b.getStatus());
        if (b.getCreatedAt() != null) data.put("createdAt", b.getCreatedAt().toString());
        if (b.getUpdatedAt() != null) data.put("updatedAt", b.getUpdatedAt().toString());
        return data;
    }

    private BookingModel fromMap(Map<String, Object> data) {
        BookingModel b = new BookingModel();
        b.setBookingId((String) data.getOrDefault("bookingId", data.get("id")));
        b.setCustomerId((String) data.get("customerId"));
        b.setVehicleId((String) data.get("vehicleId"));
        b.setServiceType((String) data.get("serviceType"));
        b.setDescription((String) data.get("description"));
        String bookingDate = (String) data.get("bookingDate");
        if (bookingDate != null) b.setBookingDate(java.time.LocalDate.parse(bookingDate));
        b.setPreferredTime((String) data.get("preferredTime"));
        b.setStatus((String) data.get("status"));
        String createdAt = (String) data.get("createdAt");
        if (createdAt != null) b.setCreatedAt(java.time.Instant.parse(createdAt));
        String updatedAt = (String) data.get("updatedAt");
        if (updatedAt != null) b.setUpdatedAt(java.time.Instant.parse(updatedAt));
        return b;
    }
}
