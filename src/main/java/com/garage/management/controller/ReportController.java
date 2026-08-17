package com.garage.management.controller;

import com.garage.management.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Garage analytics and reports")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard stats")
    public ResponseEntity<Map<String, Object>> getDashboard() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily report for a specific date")
    public ResponseEntity<Map<String, Object>> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
            throws ExecutionException, InterruptedException {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(reportService.getDailyReport(date));
    }

    @GetMapping("/bays")
    @Operation(summary = "Get bay utilization report")
    public ResponseEntity<List<Map<String, Object>>> getBayReport() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reportService.getBayUtilizationReport());
    }

    @GetMapping("/mechanics")
    @Operation(summary = "Get mechanic workload report")
    public ResponseEntity<List<Map<String, Object>>> getMechanicReport() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reportService.getMechanicWorkloadReport());
    }

    @GetMapping("/service-types")
    @Operation(summary = "Get service type statistics")
    public ResponseEntity<Map<String, Long>> getServiceTypeStats() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reportService.getServiceTypeStats());
    }
}
