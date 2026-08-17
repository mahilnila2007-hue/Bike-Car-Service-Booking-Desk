package com.garage.management.controller;

import com.garage.management.model.MechanicModel;
import com.garage.management.service.MechanicAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/mechanics")
@Tag(name = "Mechanics", description = "Mechanic management")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class MechanicController {

    private final MechanicAssignmentService mechanicService;

    public MechanicController(MechanicAssignmentService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @GetMapping
    @Operation(summary = "List all mechanics")
    public ResponseEntity<List<MechanicModel>> getAllMechanics() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(mechanicService.getAllMechanics());
    }

    @GetMapping("/available")
    @Operation(summary = "List available mechanics")
    public ResponseEntity<List<MechanicModel>> getAvailableMechanics() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(mechanicService.getAvailableMechanics());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mechanic by ID")
    public ResponseEntity<MechanicModel> getMechanic(@PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(mechanicService.getMechanic(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new mechanic (Admin)")
    public ResponseEntity<MechanicModel> createMechanic(@RequestBody MechanicModel mechanic)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(mechanicService.saveMechanic(mechanic));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update mechanic availability status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        mechanicService.updateMechanicStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Mechanic status updated", "mechanicId", id));
    }
}
