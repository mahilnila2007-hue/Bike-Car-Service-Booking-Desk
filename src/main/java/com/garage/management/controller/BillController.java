package com.garage.management.controller;

import com.garage.management.model.BillModel;
import com.garage.management.model.ServicePartModel;
import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.security.UserRole;
import com.garage.management.service.BillingService;
import com.garage.management.service.PartsService;
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
@RequestMapping("/api/bills")
@Tag(name = "Billing", description = "Billing and payment management")
public class BillController {

    private final BillingService billingService;
    private final PartsService partsService;

    public BillController(BillingService billingService, PartsService partsService) {
        this.billingService = billingService;
        this.partsService = partsService;
    }

    @PostMapping
    @Operation(summary = "Generate bill for a service job (Staff/Admin)")
    public ResponseEntity<BillModel> generateBill(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        String serviceJobId = (String) body.get("serviceJobId");
        double labourCost = ((Number) body.getOrDefault("labourCost", 0)).doubleValue();
        double discount = ((Number) body.getOrDefault("discount", 0)).doubleValue();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.generateBill(serviceJobId, labourCost, discount));
    }

    @GetMapping
    @Operation(summary = "List bills")
    public ResponseEntity<List<BillModel>> getBills(
            @AuthenticationPrincipal FirebaseUserDetails userDetails)
            throws ExecutionException, InterruptedException {
        if (isStaffOrAdmin(userDetails)) return ResponseEntity.ok(billingService.getAllBills());
        return ResponseEntity.ok(billingService.getBillsByCustomer(userDetails.getUid()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<BillModel> getBill(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        BillModel bill = billingService.getBill(id);
        if (!isStaffOrAdmin(userDetails) && !bill.getCustomerId().equals(userDetails.getUid())) {
            throw new ForbiddenException("You can only view your own bills.");
        }
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/service-job/{serviceJobId}")
    @Operation(summary = "Get bill by service job ID")
    public ResponseEntity<BillModel> getBillByJob(@PathVariable String serviceJobId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(billingService.getBillByServiceJob(serviceJobId));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Mark bill as paid (Staff/Admin)")
    public ResponseEntity<BillModel> markPaid(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(billingService.markPaid(id));
    }

    @PostMapping("/service-job/{serviceJobId}/parts")
    @Operation(summary = "Add a part usage to a service job (Staff/Admin)")
    public ResponseEntity<ServicePartModel> addPart(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String serviceJobId,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        String partId = (String) body.get("partId");
        int quantity = ((Number) body.getOrDefault("quantity", 1)).intValue();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partsService.usePartForJob(serviceJobId, partId, quantity));
    }

    @GetMapping("/service-job/{serviceJobId}/parts")
    @Operation(summary = "Get parts used in a service job")
    public ResponseEntity<List<ServicePartModel>> getParts(@PathVariable String serviceJobId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(partsService.getPartsForJob(serviceJobId));
    }

    private boolean isStaffOrAdmin(FirebaseUserDetails user) {
        return user.getRole() == UserRole.STAFF || user.getRole() == UserRole.ADMIN;
    }

    private void requireStaffOrAdmin(FirebaseUserDetails user) {
        if (!isStaffOrAdmin(user)) throw new ForbiddenException("Staff or Admin access required.");
    }
}
