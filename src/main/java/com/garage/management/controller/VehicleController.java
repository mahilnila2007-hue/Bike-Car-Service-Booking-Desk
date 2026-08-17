package com.garage.management.controller;

import com.garage.management.model.VehicleModel;
import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Vehicle management")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Add a new vehicle")
    public ResponseEntity<VehicleModel> addVehicle(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {

        String customerId = isStaffOrAdmin(userDetails)
                ? (String) body.getOrDefault("customerId", userDetails.getUid())
                : userDetails.getUid();

        VehicleModel vehicle = vehicleService.addVehicle(
                customerId,
                (String) body.get("registrationNumber"),
                (String) body.get("vehicleType"),
                (String) body.get("brand"),
                (String) body.get("model"),
                body.get("manufacturingYear") != null ? ((Number) body.get("manufacturingYear")).intValue() : 2020,
                (String) body.get("fuelType"),
                body.get("mileage") != null ? ((Number) body.get("mileage")).intValue() : 0
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @GetMapping
    @Operation(summary = "List vehicles (own vehicles for customers, all for staff/admin)")
    public ResponseEntity<List<VehicleModel>> getVehicles(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestParam(required = false) String customerId) throws ExecutionException, InterruptedException {

        if (isStaffOrAdmin(userDetails)) {
            if (customerId != null) return ResponseEntity.ok(vehicleService.getVehiclesByCustomer(customerId));
            return ResponseEntity.ok(vehicleService.getAllVehicles());
        }
        return ResponseEntity.ok(vehicleService.getVehiclesByCustomer(userDetails.getUid()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<VehicleModel> getVehicle(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(
                vehicleService.getVehicle(id, userDetails.getUid(), isStaffOrAdmin(userDetails)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle details")
    public ResponseEntity<VehicleModel> updateVehicle(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {

        return ResponseEntity.ok(vehicleService.updateVehicle(
                id, userDetails.getUid(), isStaffOrAdmin(userDetails),
                (String) body.get("brand"),
                (String) body.get("model"),
                body.get("mileage") != null ? ((Number) body.get("mileage")).intValue() : 0
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle")
    public ResponseEntity<Map<String, String>> deleteVehicle(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        vehicleService.deleteVehicle(id, userDetails.getUid(), isStaffOrAdmin(userDetails));
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted", "vehicleId", id));
    }

    private boolean isStaffOrAdmin(FirebaseUserDetails user) {
        var role = user.getRole();
        return role == com.garage.management.security.UserRole.STAFF
                || role == com.garage.management.security.UserRole.ADMIN;
    }
}
