package com.garage.management.controller;

import com.garage.management.model.ServiceJobModel;
import com.garage.management.model.ServiceStatusHistoryModel;
import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.security.UserRole;
import com.garage.management.service.ServiceJobService;
import com.garage.management.service.ServiceStatusHistoryService;
import com.garage.management.exception.ForbiddenException;
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
@RequestMapping("/api/service-jobs")
@Tag(name = "Service Jobs", description = "Service job lifecycle management")
public class ServiceJobController {

    private final ServiceJobService serviceJobService;
    private final ServiceStatusHistoryService historyService;

    public ServiceJobController(ServiceJobService serviceJobService,
                                ServiceStatusHistoryService historyService) {
        this.serviceJobService = serviceJobService;
        this.historyService = historyService;
    }

    @PostMapping
    @Operation(summary = "Create service job from a confirmed booking (Staff/Admin)")
    public ResponseEntity<ServiceJobModel> createServiceJob(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                serviceJobService.createServiceJob(body.get("bookingId"), userDetails.getUid()));
    }

    @GetMapping
    @Operation(summary = "List service jobs (own for customers, all for staff/admin)")
    public ResponseEntity<List<ServiceJobModel>> getJobs(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestParam(required = false) String status) throws ExecutionException, InterruptedException {

        if (isStaffOrAdmin(userDetails)) {
            if (status != null) return ResponseEntity.ok(serviceJobService.getJobsByStatus(status));
            return ResponseEntity.ok(serviceJobService.getAllJobs());
        }
        return ResponseEntity.ok(serviceJobService.getJobsByCustomer(userDetails.getUid()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service job by ID")
    public ResponseEntity<ServiceJobModel> getJob(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        ServiceJobModel job = serviceJobService.getJobById(id);
        if (!isStaffOrAdmin(userDetails) && !job.getCustomerId().equals(userDetails.getUid())) {
            throw new ForbiddenException("You can only view your own service jobs.");
        }
        return ResponseEntity.ok(job);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get service job by booking ID")
    public ResponseEntity<ServiceJobModel> getJobByBooking(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String bookingId) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(serviceJobService.getJobByBookingId(bookingId));
    }

    @PostMapping("/{id}/allocate-bay")
    @Operation(summary = "Auto-allocate bay to service job (Staff/Admin)")
    public ResponseEntity<ServiceJobModel> allocateBay(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(serviceJobService.allocateBay(id, userDetails.getUid()));
    }

    @PostMapping("/{id}/assign-mechanic")
    @Operation(summary = "Assign mechanic to service job (Staff/Admin)")
    public ResponseEntity<ServiceJobModel> assignMechanic(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        String mechanicId = body != null ? body.get("mechanicId") : null;
        return ResponseEntity.ok(serviceJobService.assignMechanic(id, mechanicId, userDetails.getUid()));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start service (Staff/Admin)")
    public ResponseEntity<ServiceJobModel> startService(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(serviceJobService.startService(id, userDetails.getUid()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update service status (Staff/Admin)")
    public ResponseEntity<ServiceJobModel> updateStatus(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(serviceJobService.updateStatus(
                id, body.get("status"), body.get("remarks"), userDetails.getUid()));
    }

    @PatchMapping("/{id}/notes")
    @Operation(summary = "Update service notes (Staff/Admin)")
    public ResponseEntity<Map<String, String>> updateNotes(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        serviceJobService.updateNotes(id, body.get("notes"));
        return ResponseEntity.ok(Map.of("message", "Notes updated"));
    }

    @PatchMapping("/{id}/eta")
    @Operation(summary = "Update ETA (Staff/Admin)")
    public ResponseEntity<ServiceJobModel> updateEta(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(serviceJobService.updateEta(id, body.get("eta"), userDetails.getUid()));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get status history for a service job")
    public ResponseEntity<List<ServiceStatusHistoryModel>> getHistory(
            @PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(historyService.getHistoryForJob(id));
    }

    private boolean isStaffOrAdmin(FirebaseUserDetails user) {
        return user.getRole() == UserRole.STAFF || user.getRole() == UserRole.ADMIN;
    }

    private void requireStaffOrAdmin(FirebaseUserDetails user) {
        if (!isStaffOrAdmin(user)) throw new ForbiddenException("Staff or Admin access required.");
    }
}
