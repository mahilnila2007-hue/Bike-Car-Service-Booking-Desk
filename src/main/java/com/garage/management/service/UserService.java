package com.garage.management.service;

import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.UserModel;
import com.garage.management.repository.UserRepository;
import com.garage.management.util.DateTimeUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Service for Firebase Authentication and user management.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    public UserService(UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Create or update user profile in Firestore after Firebase Authentication.
     * Called after the frontend registers/logs in and sends the Firebase ID token.
     */
    public UserModel createOrUpdateProfile(String uid, String name, String email, String phone, String role)
            throws ExecutionException, InterruptedException {

        Optional<UserModel> existing = userRepository.findById(uid);

        if (existing.isPresent()) {
            // Update if profile already exists
            UserModel user = existing.get();
            if (name != null) user.setName(name);
            if (phone != null) user.setPhone(phone);
            user.setUpdatedAt(DateTimeUtil.now());
            userRepository.save(user);
            log.info("Updated profile for user: {}", uid);
            return user;
        }

        // Create new profile
        UserModel user = new UserModel();
        user.setUid(uid);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role != null ? role : "CUSTOMER");
        user.setActive(true);
        user.setCreatedAt(DateTimeUtil.now());
        user.setUpdatedAt(DateTimeUtil.now());

        userRepository.save(user);
        log.info("Created profile for user: {} with role: {}", uid, user.getRole());
        return user;
    }

    public UserModel getProfile(String uid) throws ExecutionException, InterruptedException {
        return userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uid));
    }

    public List<UserModel> getAllUsers() throws ExecutionException, InterruptedException {
        return userRepository.findAll();
    }

    public List<UserModel> getUsersByRole(String role) throws ExecutionException, InterruptedException {
        return userRepository.findByRole(role);
    }

    public UserModel updateProfile(String uid, String name, String phone)
            throws ExecutionException, InterruptedException {
        UserModel user = getProfile(uid);
        if (name != null && !name.isBlank()) user.setName(name);
        if (phone != null && !phone.isBlank()) user.setPhone(phone);
        user.setUpdatedAt(DateTimeUtil.now());
        userRepository.save(user);
        return user;
    }

    public void updateUserRole(String uid, String newRole)
            throws ExecutionException, InterruptedException {
        // Ensure user exists
        getProfile(uid);
        Map<String, Object> fields = new HashMap<>();
        fields.put("role", newRole);
        fields.put("updatedAt", DateTimeUtil.now().toString());
        userRepository.update(uid, fields);
        log.info("Updated role for user {} to {}", uid, newRole);
    }

    public void deactivateUser(String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        // Disable in Firebase Auth
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(true);
        firebaseAuth.updateUser(request);

        // Update Firestore
        Map<String, Object> fields = new HashMap<>();
        fields.put("active", false);
        fields.put("updatedAt", DateTimeUtil.now().toString());
        userRepository.update(uid, fields);
        log.info("Deactivated user: {}", uid);
    }
}
