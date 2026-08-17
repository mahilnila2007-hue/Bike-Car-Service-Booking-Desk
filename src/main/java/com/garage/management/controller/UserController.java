package com.garage.management.controller;

import com.garage.management.model.UserModel;
import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Admin user management")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all users (Admin)")
    public ResponseEntity<List<UserModel>> getAllUsers() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{uid}")
    @Operation(summary = "Get user by UID")
    public ResponseEntity<UserModel> getUser(@PathVariable String uid) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(userService.getProfile(uid));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "List users by role")
    public ResponseEntity<List<UserModel>> getUsersByRole(@PathVariable String role)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PatchMapping("/{uid}/role")
    @Operation(summary = "Update user role (Admin only)")
    public ResponseEntity<Map<String, String>> updateRole(
            @PathVariable String uid,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String newRole = body.get("role");
        userService.updateUserRole(uid, newRole);
        return ResponseEntity.ok(Map.of("message", "Role updated to " + newRole, "uid", uid));
    }

    @DeleteMapping("/{uid}/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable String uid)
            throws Exception {
        userService.deactivateUser(uid);
        return ResponseEntity.ok(Map.of("message", "User deactivated", "uid", uid));
    }
}
