package com.garage.management.controller;

import com.garage.management.service.SeedDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/seed")
@Tag(name = "Seed Data", description = "Demo data seeding — Admin only")
@PreAuthorize("hasRole('ADMIN')")
public class SeedController {

    private final SeedDataService seedDataService;

    public SeedController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping
    @Operation(summary = "Seed demo data: bays, mechanics, service types, parts (Admin only)")
    public ResponseEntity<Map<String, Object>> seedAll() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(seedDataService.seedAll());
    }
}
