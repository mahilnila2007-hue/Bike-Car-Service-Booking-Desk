package com.garage.management.controller;

import com.garage.management.model.UserModel;
import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User profile management after Firebase Authentication")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/profile")
    @Operation(summary = "Create or update user profile in Firestore after Firebase login")
    public ResponseEntity<UserModel> createOrUpdateProfile(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {

        String name = body.get("name");
        String phone = body.get("phone");
        String role = body.getOrDefault("role", "CUSTOMER");

        // Only allow CUSTOMER during self-registration
        // STAFF and ADMIN must be set by admin
        if ("STAFF".equals(role) || "ADMIN".equals(role)) {
            role = "CUSTOMER";
        }

        UserModel user = userService.createOrUpdateProfile(
                userDetails.getUid(), name, userDetails.getUsername(), phone, role);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<UserModel> getProfile(
            @AuthenticationPrincipal FirebaseUserDetails userDetails) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUid()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user's name and phone")
    public ResponseEntity<UserModel> updateProfile(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(
                userService.updateProfile(userDetails.getUid(), body.get("name"), body.get("phone")));
    }
}
