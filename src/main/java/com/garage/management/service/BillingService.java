package com.garage.management.service;

import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.BillModel;
import com.garage.management.model.ServiceJobModel;
import com.garage.management.repository.BillRepository;
import com.garage.management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Billing service.
 *
 * Calculation:
 *   partsCost = sum(part subtotals)
 *   subtotal  = labourCost + partsCost
 *   taxAmount = subtotal × taxPercentage / 100
 *   total     = subtotal + taxAmount - discount
 */
@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    @Value("${billing.tax.percentage:18.0}")
    private double defaultTaxPercentage;

    private final BillRepository billRepository;
    private final ServiceJobService serviceJobService;
    private final PartsService partsService;

    public BillingService(BillRepository billRepository,
                          ServiceJobService serviceJobService,
                          PartsService partsService) {
        this.billRepository = billRepository;
        this.serviceJobService = serviceJobService;
        this.partsService = partsService;
    }

    /**
     * Generate a bill for a service job.
     */
    public BillModel generateBill(String serviceJobId, double labourCost, double discount)
            throws ExecutionException, InterruptedException {

        ServiceJobModel job = serviceJobService.getJobById(serviceJobId);

        // Check if bill already exists
        billRepository.findByServiceJobId(serviceJobId).ifPresent(existing -> {
            throw new BadRequestException("Bill already exists for service job: " + serviceJobId +
                    ". Bill ID: " + existing.getBillId());
        });

        // Calculate parts cost from all service parts
        double partsCost = partsService.calculatePartsCostForJob(serviceJobId);
        double subtotal = labourCost + partsCost;
        double taxAmount = Math.round(subtotal * defaultTaxPercentage) / 100.0;
        double totalAmount = subtotal + taxAmount - discount;

        if (totalAmount < 0) totalAmount = 0;

        BillModel bill = new BillModel();
        bill.setBillId(billRepository.generateId());
        bill.setBookingId(job.getBookingId());
        bill.setServiceJobId(serviceJobId);
        bill.setCustomerId(job.getCustomerId());
        bill.setLabourCost(labourCost);
        bill.setPartsCost(partsCost);
        bill.setTaxPercentage(defaultTaxPercentage);
        bill.setTaxAmount(taxAmount);
        bill.setDiscount(discount);
        bill.setTotalAmount(totalAmount);
        bill.setPaymentStatus("PENDING");
        bill.setGeneratedAt(DateTimeUtil.now());

        billRepository.save(bill);
        log.info("Generated bill {} for service job {}. Total: ₹{}", bill.getBillId(), serviceJobId, totalAmount);
        return bill;
    }

    /**
     * Mark bill as PAID.
     */
    public BillModel markPaid(String billId) throws ExecutionException, InterruptedException {
        BillModel bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));

        if ("PAID".equals(bill.getPaymentStatus())) {
            throw new BadRequestException("Bill is already marked as PAID.");
        }

        bill.setPaymentStatus("PAID");
        bill.setPaidAt(DateTimeUtil.now());

        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "PAID");
        updates.put("paidAt", DateTimeUtil.now().toString());
        billRepository.update(billId, updates);

        log.info("Bill {} marked as PAID", billId);
        return bill;
    }

    public BillModel getBill(String billId) throws ExecutionException, InterruptedException {
        return billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));
    }

    public BillModel getBillByServiceJob(String serviceJobId) throws ExecutionException, InterruptedException {
        return billRepository.findByServiceJobId(serviceJobId)
                .orElseThrow(() -> new ResourceNotFoundException("No bill for service job: " + serviceJobId));
    }

    public List<BillModel> getAllBills() throws ExecutionException, InterruptedException {
        return billRepository.findAll();
    }

    public List<BillModel> getBillsByCustomer(String customerId) throws ExecutionException, InterruptedException {
        return billRepository.findByCustomerId(customerId);
    }

    public List<BillModel> getPendingBills() throws ExecutionException, InterruptedException {
        return billRepository.findByPaymentStatus("PENDING");
    }

    public double getTotalRevenue() throws ExecutionException, InterruptedException {
        return billRepository.findByPaymentStatus("PAID").stream()
                .mapToDouble(BillModel::getTotalAmount).sum();
    }
}
