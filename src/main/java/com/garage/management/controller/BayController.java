package com.garage.management.controller;

import com.garage.management.model.BayModel;
import com.garage.management.service.BayAllocationService;
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
@RequestMapping("/api/bays")
@Tag(name = "Bays", description = "Service bay management")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class BayController {

    private final BayAllocationService bayAllocationService;

    public BayController(BayAllocationService bayAllocationService) {
        this.bayAllocationService = bayAllocationService;
    }

    @GetMapping
    @Operation(summary = "List all bays")
    public ResponseEntity<List<BayModel>> getAllBays() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(bayAllocationService.getAllBays());
    }

    @GetMapping("/available")
    @Operation(summary = "List available bays")
    public ResponseEntity<List<BayModel>> getAvailableBays() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(bayAllocationService.getAvailableBays());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bay by ID")
    public ResponseEntity<BayModel> getBay(@PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(bayAllocationService.getBay(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new bay (Admin)")
    public ResponseEntity<BayModel> createBay(@RequestBody BayModel bay) throws ExecutionException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(bayAllocationService.saveBay(bay));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update bay status (AVAILABLE/OCCUPIED/MAINTENANCE)")
    public ResponseEntity<Map<String, String>> updateBayStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        bayAllocationService.updateBayStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Bay status updated", "bayId", id));
    }
}
