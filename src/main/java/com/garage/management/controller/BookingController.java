package com.garage.management.controller;

import com.garage.management.model.BookingModel;
import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.security.UserRole;
import com.garage.management.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Service booking management")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a new service booking")
    public ResponseEntity<BookingModel> createBooking(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestBody Map<String, Object> body) throws ExecutionException, InterruptedException {

        String customerId = isStaffOrAdmin(userDetails)
                ? (String) body.getOrDefault("customerId", userDetails.getUid())
                : userDetails.getUid();

        LocalDate bookingDate = LocalDate.parse((String) body.get("bookingDate"));

        BookingModel booking = bookingService.createBooking(
                customerId,
                (String) body.get("vehicleId"),
                (String) body.get("serviceType"),
                (String) body.get("description"),
                bookingDate,
                (String) body.get("preferredTime")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping
    @Operation(summary = "List bookings (own for customers, all for staff/admin)")
    public ResponseEntity<List<BookingModel>> getBookings(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerId) throws ExecutionException, InterruptedException {

        if (isStaffOrAdmin(userDetails)) {
            if (status != null) return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
            if (customerId != null) return ResponseEntity.ok(bookingService.getBookingsByCustomer(customerId));
            return ResponseEntity.ok(bookingService.getAllBookings());
        }
        return ResponseEntity.ok(bookingService.getBookingsByCustomer(userDetails.getUid()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingModel> getBooking(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(
                bookingService.getBooking(id, userDetails.getUid(), isStaffOrAdmin(userDetails)));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking (Staff/Admin)")
    public ResponseEntity<BookingModel> confirmBooking(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Mark vehicle as received (Staff/Admin)")
    public ResponseEntity<BookingModel> receiveVehicle(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        requireStaffOrAdmin(userDetails);
        return ResponseEntity.ok(bookingService.receiveVehicle(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @AuthenticationPrincipal FirebaseUserDetails userDetails,
            @PathVariable String id) throws ExecutionException, InterruptedException {
        bookingService.cancelBooking(id, userDetails.getUid(), isStaffOrAdmin(userDetails));
        return ResponseEntity.ok(Map.of("message", "Booking cancelled", "bookingId", id));
    }

    private boolean isStaffOrAdmin(FirebaseUserDetails user) {
        return user.getRole() == UserRole.STAFF || user.getRole() == UserRole.ADMIN;
    }

    private void requireStaffOrAdmin(FirebaseUserDetails user) {
        if (!isStaffOrAdmin(user)) {
            throw new com.garage.management.exception.ForbiddenException("Only staff or admin can perform this action.");
        }
    }
}
