package com.garage.management.repository;

import com.garage.management.model.UserModel;
import com.garage.management.security.UserRole;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * User-specific Firestore repository operations.
 */
@Repository
public class UserRepository {

    private static final String COLLECTION = "users";

    private final FirestoreRepository repo;

    public UserRepository(FirestoreRepository repo) {
        this.repo = repo;
    }

    public void save(UserModel user) throws ExecutionException, InterruptedException {
        Map<String, Object> data = toMap(user);
        repo.save(COLLECTION, user.getUid(), data);
    }

    public Optional<UserModel> findById(String uid) throws ExecutionException, InterruptedException {
        return repo.findById(COLLECTION, uid).map(this::fromMap);
    }

    public List<UserModel> findAll() throws ExecutionException, InterruptedException {
        return repo.findAll(COLLECTION).stream().map(this::fromMap).toList();
    }

    public List<UserModel> findByRole(String role) throws ExecutionException, InterruptedException {
        return repo.findByField(COLLECTION, "role", role).stream().map(this::fromMap).toList();
    }

    public boolean existsByEmail(String email) throws ExecutionException, InterruptedException {
        return repo.existsByField(COLLECTION, "email", email);
    }

    public void update(String uid, Map<String, Object> fields) throws ExecutionException, InterruptedException {
        repo.update(COLLECTION, uid, fields);
    }

    /**
     * Get user role from Firestore. Returns CUSTOMER if user not found (default).
     */
    public UserRole getUserRole(String uid) {
        try {
            Optional<Map<String, Object>> doc = repo.findById(COLLECTION, uid);
            if (doc.isPresent()) {
                String roleStr = (String) doc.get().get("role");
                if (roleStr != null) {
                    return UserRole.valueOf(roleStr);
                }
            }
        } catch (Exception e) {
            // Log and default to CUSTOMER for safety
        }
        return UserRole.CUSTOMER;
    }

    private Map<String, Object> toMap(UserModel user) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("role", user.getRole());
        data.put("active", user.isActive());
        if (user.getCreatedAt() != null) data.put("createdAt", user.getCreatedAt().toString());
        if (user.getUpdatedAt() != null) data.put("updatedAt", user.getUpdatedAt().toString());
        return data;
    }

    private UserModel fromMap(Map<String, Object> data) {
        UserModel user = new UserModel();
        user.setUid((String) data.get("uid"));
        user.setName((String) data.get("name"));
        user.setEmail((String) data.get("email"));
        user.setPhone((String) data.get("phone"));
        user.setRole((String) data.get("role"));
        Object active = data.get("active");
        user.setActive(active != null && (boolean) active);
        String createdAt = (String) data.get("createdAt");
        if (createdAt != null) user.setCreatedAt(java.time.Instant.parse(createdAt));
        String updatedAt = (String) data.get("updatedAt");
        if (updatedAt != null) user.setUpdatedAt(java.time.Instant.parse(updatedAt));
        return user;
    }
}
