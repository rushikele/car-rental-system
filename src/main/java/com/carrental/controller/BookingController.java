package com.carrental.controller;

import com.carrental.dto.request.BookingRequest;
import com.carrental.dto.response.ApiResponse;
import com.carrental.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Create booking (payment still pending after this)
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking created! Proceed to payment.",
                bookingService.createBooking(auth.getName(), request)));
    }

    // Get logged-in user's bookings
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyBookings(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Your bookings", bookingService.getMyBookings(auth.getName())));
    }

    // Get specific booking (user can only see own)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBookingById(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking details", bookingService.getBookingById(id, auth.getName())));
    }

    // Cancel booking
    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<?>> cancelBooking(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking cancelled successfully!",
                bookingService.cancelBooking(id, auth.getName())));
    }

    // Admin: Get all bookings
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.success("All bookings", bookingService.getAllBookings()));
    }

    // Admin: Mark booking as completed
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/complete/{id}")
    public ResponseEntity<ApiResponse<?>> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking marked as completed!", bookingService.completeBooking(id)));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBooking(
            @PathVariable Long id, Authentication auth) {
        bookingService.deleteBooking(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success(
                "Booking removed from your history."));
    }
}
