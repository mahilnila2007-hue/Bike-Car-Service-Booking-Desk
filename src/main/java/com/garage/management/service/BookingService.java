package com.garage.management.service;

import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.ForbiddenException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.BookingModel;
import com.garage.management.model.VehicleModel;
import com.garage.management.repository.BookingRepository;
import com.garage.management.repository.VehicleRepository;
import com.garage.management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final List<String> CANCELLABLE_STATUSES = List.of("PENDING", "CONFIRMED");
    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "CONFIRMED", "VEHICLE_RECEIVED");

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;

    public BookingService(BookingRepository bookingRepository, VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public BookingModel createBooking(String customerId, String vehicleId, String serviceType,
                                      String description, LocalDate bookingDate, String preferredTime)
            throws ExecutionException, InterruptedException {

        // Verify vehicle belongs to customer
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (!vehicle.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("Vehicle does not belong to this customer.");
        }

        // Business Rule: Prevent duplicate active bookings for same vehicle on same date
        List<BookingModel> existing = bookingRepository.findByVehicleId(vehicleId);
        boolean hasDuplicate = existing.stream()
                .anyMatch(b -> ACTIVE_STATUSES.contains(b.getStatus())
                        && b.getBookingDate() != null
                        && b.getBookingDate().equals(bookingDate));

        if (hasDuplicate) {
            throw new BadRequestException(
                    "An active booking already exists for this vehicle on " + bookingDate + ".");
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Booking date cannot be in the past.");
        }

        BookingModel booking = new BookingModel();
        booking.setBookingId(bookingRepository.generateId());
        booking.setCustomerId(customerId);
        booking.setVehicleId(vehicleId);
        booking.setServiceType(serviceType);
        booking.setDescription(description);
        booking.setBookingDate(bookingDate);
        booking.setPreferredTime(preferredTime);
        booking.setStatus("PENDING");
        booking.setCreatedAt(DateTimeUtil.now());
        booking.setUpdatedAt(DateTimeUtil.now());

        bookingRepository.save(booking);
        log.info("Created booking {} for customer {} vehicle {}", booking.getBookingId(), customerId, vehicleId);
        return booking;
    }

    public BookingModel getBooking(String bookingId, String requesterId, boolean isStaffOrAdmin)
            throws ExecutionException, InterruptedException {
        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!isStaffOrAdmin && !booking.getCustomerId().equals(requesterId)) {
            throw new ForbiddenException("You can only access your own bookings.");
        }
        return booking;
    }

    public List<BookingModel> getBookingsByCustomer(String customerId) throws ExecutionException, InterruptedException {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<BookingModel> getAllBookings() throws ExecutionException, InterruptedException {
        return bookingRepository.findAll();
    }

    public List<BookingModel> getBookingsByStatus(String status) throws ExecutionException, InterruptedException {
        return bookingRepository.findByStatus(status);
    }

    public BookingModel confirmBooking(String bookingId) throws ExecutionException, InterruptedException {
        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Only PENDING bookings can be confirmed. Current status: " + booking.getStatus());
        }

        booking.setStatus("CONFIRMED");
        booking.setUpdatedAt(DateTimeUtil.now());

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CONFIRMED");
        updates.put("updatedAt", DateTimeUtil.now().toString());
        bookingRepository.update(bookingId, updates);

        log.info("Confirmed booking: {}", bookingId);
        return booking;
    }

    public BookingModel receiveVehicle(String bookingId) throws ExecutionException, InterruptedException {
        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Only CONFIRMED bookings can be received. Current status: " + booking.getStatus());
        }

        booking.setStatus("VEHICLE_RECEIVED");
        booking.setUpdatedAt(DateTimeUtil.now());

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "VEHICLE_RECEIVED");
        updates.put("updatedAt", DateTimeUtil.now().toString());
        bookingRepository.update(bookingId, updates);

        log.info("Vehicle received for booking: {}", bookingId);
        return booking;
    }

    public void cancelBooking(String bookingId, String requesterId, boolean isStaffOrAdmin)
            throws ExecutionException, InterruptedException {
        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Business Rule: Completed bookings cannot be cancelled
        if (!CANCELLABLE_STATUSES.contains(booking.getStatus())) {
            throw new BadRequestException("Cannot cancel booking with status: " + booking.getStatus() +
                    ". Only PENDING or CONFIRMED bookings can be cancelled.");
        }

        if (!isStaffOrAdmin && !booking.getCustomerId().equals(requesterId)) {
            throw new ForbiddenException("You can only cancel your own bookings.");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELLED");
        updates.put("updatedAt", DateTimeUtil.now().toString());
        bookingRepository.update(bookingId, updates);

        log.info("Cancelled booking: {}", bookingId);
    }

    public void markCompleted(String bookingId) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "COMPLETED");
        updates.put("updatedAt", DateTimeUtil.now().toString());
        bookingRepository.update(bookingId, updates);
    }
}
