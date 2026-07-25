package com.carrental.controller;

import com.carrental.dto.request.PaymentVerifyRequest;
import com.carrental.dto.response.ApiResponse;
import com.carrental.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // ✅ STEP 1: Create Razorpay order for a booking
    // Frontend calls this → gets orderId → opens Razorpay popup
    @PostMapping("/create-order/{bookingId}")
    public ResponseEntity<ApiResponse<?>> createOrder(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment order created!", paymentService.createPaymentOrder(bookingId)));
    }

    // ✅ STEP 2: Verify payment after Razorpay processes it
    // Frontend sends back razorpayOrderId, razorpayPaymentId, razorpaySignature
    // We verify signature → confirm booking → send email
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment verified! Booking confirmed. Check your email.",
                paymentService.verifyAndConfirmPayment(request)));
    }
}
