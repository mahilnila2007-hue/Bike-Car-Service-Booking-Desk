package com.garage.management.controller;

import com.garage.management.model.PartModel;
import com.garage.management.service.PartsService;
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
@RequestMapping("/api/parts")
@Tag(name = "Parts", description = "Parts inventory management")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class PartController {

    private final PartsService partsService;

    public PartController(PartsService partsService) {
        this.partsService = partsService;
    }

    @GetMapping
    @Operation(summary = "List all parts")
    public ResponseEntity<List<PartModel>> getAllParts() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(partsService.getAllParts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get part by ID")
    public ResponseEntity<PartModel> getPart(@PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(partsService.getPart(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new part (Admin)")
    public ResponseEntity<PartModel> createPart(@RequestBody Map<String, Object> body)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(partsService.createPart(
                (String) body.get("partName"),
                (String) body.get("partNumber"),
                ((Number) body.getOrDefault("price", 0)).doubleValue(),
                ((Number) body.getOrDefault("stockQuantity", 0)).intValue()
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update part details")
    public ResponseEntity<PartModel> updatePart(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(partsService.updatePart(
                id,
                (String) body.get("partName"),
                ((Number) body.getOrDefault("price", 0)).doubleValue(),
                ((Number) body.getOrDefault("stockQuantity", -1)).intValue()
        ));
    }
}
