package com.garage.management.controller;

import com.garage.management.model.ServiceTypeModel;
import com.garage.management.service.ServiceTypeService;
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
@RequestMapping("/api/service-types")
@Tag(name = "Service Types", description = "Service type catalog management")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @GetMapping
    @Operation(summary = "List all active service types")
    public ResponseEntity<List<ServiceTypeModel>> getAll() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(serviceTypeService.getActive());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @Operation(summary = "List all service types including inactive (Staff/Admin)")
    public ResponseEntity<List<ServiceTypeModel>> getAllIncludingInactive() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(serviceTypeService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service type by ID")
    public ResponseEntity<ServiceTypeModel> getById(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(serviceTypeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create service type (Admin)")
    public ResponseEntity<ServiceTypeModel> create(@RequestBody Map<String, Object> body)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceTypeService.create(
                (String) body.get("name"),
                (String) body.get("description"),
                ((Number) body.getOrDefault("estimatedDurationMinutes", 60)).intValue(),
                ((Number) body.getOrDefault("defaultLabourCost", 0)).doubleValue()
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update service type (Admin)")
    public ResponseEntity<ServiceTypeModel> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(serviceTypeService.update(
                id,
                (String) body.get("name"),
                (String) body.get("description"),
                ((Number) body.getOrDefault("estimatedDurationMinutes", 0)).intValue(),
                ((Number) body.getOrDefault("defaultLabourCost", 0)).doubleValue(),
                (Boolean) body.getOrDefault("active", true)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete service type (Admin)")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id)
            throws ExecutionException, InterruptedException {
        serviceTypeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Service type deleted", "id", id));
    }
}
