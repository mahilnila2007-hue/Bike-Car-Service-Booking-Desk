package com.garage.management.service;

import com.garage.management.model.*;
import com.garage.management.repository.*;
import com.garage.management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Reporting service for admin and staff dashboards.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final BookingRepository bookingRepository;
    private final ServiceJobRepository serviceJobRepository;
    private final BayRepository bayRepository;
    private final MechanicRepository mechanicRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public ReportService(BookingRepository bookingRepository,
                         ServiceJobRepository serviceJobRepository,
                         BayRepository bayRepository,
                         MechanicRepository mechanicRepository,
                         BillRepository billRepository,
                         UserRepository userRepository,
                         VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceJobRepository = serviceJobRepository;
        this.bayRepository = bayRepository;
        this.mechanicRepository = mechanicRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Build admin dashboard stats.
     */
    public Map<String, Object> getDashboardStats() throws ExecutionException, InterruptedException {
        Map<String, Object> stats = new LinkedHashMap<>();

        List<UserModel> users = userRepository.findAll();
        stats.put("totalCustomers", users.stream().filter(u -> "CUSTOMER".equals(u.getRole())).count());
        stats.put("totalStaff", users.stream().filter(u -> "STAFF".equals(u.getRole())).count());

        stats.put("totalVehicles", vehicleRepository.findAll().size());

        List<BookingModel> bookings = bookingRepository.findAll();
        LocalDate today = LocalDate.now();
        stats.put("totalBookings", bookings.size());
        stats.put("todayBookings", bookings.stream()
                .filter(b -> b.getBookingDate() != null && b.getBookingDate().equals(today)).count());
        stats.put("pendingBookings", bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count());
        stats.put("confirmedBookings", bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count());
        stats.put("completedBookings", bookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count());
        stats.put("cancelledBookings", bookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count());

        List<ServiceJobModel> jobs = serviceJobRepository.findAll();
        stats.put("activeServices", jobs.stream()
                .filter(j -> !List.of("COMPLETED").contains(j.getCurrentStatus())).count());
        stats.put("completedServices", jobs.stream().filter(j -> "COMPLETED".equals(j.getCurrentStatus())).count());

        List<BayModel> bays = bayRepository.findAll();
        stats.put("totalBays", bays.size());
        stats.put("availableBays", bays.stream().filter(b -> "AVAILABLE".equals(b.getStatus())).count());
        stats.put("occupiedBays", bays.stream().filter(b -> "OCCUPIED".equals(b.getStatus())).count());
        stats.put("maintenanceBays", bays.stream().filter(b -> "MAINTENANCE".equals(b.getStatus())).count());

        long totalActiveBays = bays.stream().filter(b -> !"MAINTENANCE".equals(b.getStatus())).count();
        long occupiedCount = bays.stream().filter(b -> "OCCUPIED".equals(b.getStatus())).count();
        double utilization = totalActiveBays > 0 ? (double) occupiedCount / totalActiveBays * 100 : 0;
        stats.put("garageUtilization", Math.round(utilization * 10.0) / 10.0);

        List<MechanicModel> mechanics = mechanicRepository.findAll();
        stats.put("totalMechanics", mechanics.size());
        stats.put("availableMechanics", mechanics.stream().filter(m -> "AVAILABLE".equals(m.getAvailabilityStatus())).count());
        stats.put("busyMechanics", mechanics.stream().filter(m -> "BUSY".equals(m.getAvailabilityStatus())).count());

        List<BillModel> bills = billRepository.findAll();
        stats.put("pendingPayments", bills.stream().filter(b -> "PENDING".equals(b.getPaymentStatus())).count());
        double revenue = bills.stream()
                .filter(b -> "PAID".equals(b.getPaymentStatus()))
                .mapToDouble(BillModel::getTotalAmount).sum();
        stats.put("totalRevenue", Math.round(revenue * 100.0) / 100.0);

        stats.put("readyForDelivery", jobs.stream()
                .filter(j -> "READY_FOR_DELIVERY".equals(j.getCurrentStatus())).count());

        return stats;
    }

    /**
     * Daily report: bookings, completions, revenue.
     */
    public Map<String, Object> getDailyReport(LocalDate date) throws ExecutionException, InterruptedException {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("date", date.toString());

        List<BookingModel> bookings = bookingRepository.findAll();
        List<BookingModel> todayBookings = bookings.stream()
                .filter(b -> b.getBookingDate() != null && b.getBookingDate().equals(date)).toList();

        report.put("totalBookings", todayBookings.size());
        report.put("newBookings", todayBookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count());
        report.put("confirmed", todayBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count());
        report.put("completed", todayBookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count());
        report.put("cancelled", todayBookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count());

        List<ServiceJobModel> jobs = serviceJobRepository.findAll();
        report.put("activeServices", jobs.stream()
                .filter(j -> !List.of("COMPLETED").contains(j.getCurrentStatus())).count());

        List<BillModel> bills = billRepository.findAll();
        double dayRevenue = bills.stream()
                .filter(b -> "PAID".equals(b.getPaymentStatus()))
                .mapToDouble(BillModel::getTotalAmount).sum();
        report.put("revenue", Math.round(dayRevenue * 100.0) / 100.0);

        return report;
    }

    /**
     * Bay utilization report.
     */
    public List<Map<String, Object>> getBayUtilizationReport() throws ExecutionException, InterruptedException {
        return bayRepository.findAll().stream().map(bay -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("bayId", bay.getBayId());
            entry.put("bayNumber", bay.getBayNumber());
            entry.put("bayType", bay.getBayType());
            entry.put("status", bay.getStatus());
            entry.put("currentServiceJobId", bay.getCurrentServiceJobId());
            return entry;
        }).toList();
    }

    /**
     * Mechanic workload report.
     */
    public List<Map<String, Object>> getMechanicWorkloadReport() throws ExecutionException, InterruptedException {
        return mechanicRepository.findAll().stream().map(mechanic -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("mechanicId", mechanic.getMechanicId());
            entry.put("name", mechanic.getName());
            entry.put("specialization", mechanic.getSpecialization());
            entry.put("experience", mechanic.getExperience());
            entry.put("availabilityStatus", mechanic.getAvailabilityStatus());
            return entry;
        }).toList();
    }

    /**
     * Service type statistics report.
     */
    public Map<String, Long> getServiceTypeStats() throws ExecutionException, InterruptedException {
        List<BookingModel> bookings = bookingRepository.findAll();
        return bookings.stream()
                .filter(b -> b.getServiceType() != null)
                .collect(Collectors.groupingBy(BookingModel::getServiceType, Collectors.counting()));
    }
}
