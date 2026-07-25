package com.carrental.service;

import com.carrental.dto.request.PaymentVerifyRequest;
import com.carrental.dto.response.PaymentOrderResponse;
import com.carrental.entity.Booking;
import com.carrental.dto.response.BookingResponse;
import com.carrental.exception.PaymentException;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    // ✅ STEP 1: Create Razorpay order
    // Called when user clicks "Pay Now"
    // Returns order ID which frontend uses to open Razorpay payment popup
    public PaymentOrderResponse createPaymentOrder(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));

        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            throw new PaymentException("Booking is already paid!");

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            // Razorpay takes amount in PAISE (1 INR = 100 paise)
            orderRequest.put("amount", (int)(booking.getFinalAmount() * 100));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + bookingId);

            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // Save Razorpay order ID to booking
            booking.setRazorpayOrderId(razorpayOrderId);
            bookingRepository.save(booking);

            return new PaymentOrderResponse(
                    razorpayOrderId,
                    booking.getFinalAmount(),
                    "INR",
                    bookingId,
                    razorpayKeyId  // frontend needs this to init Razorpay
            );

        } catch (RazorpayException e) {
            throw new PaymentException("Failed to create payment order: " + e.getMessage());
        }
    }

    // ✅ STEP 2: Verify payment signature
    // After user pays, Razorpay sends 3 fields back to frontend
    // Frontend sends them to this endpoint
    // We verify using HMAC-SHA256 to confirm payment is genuine (not tampered)
    @Transactional
    public BookingResponse verifyAndConfirmPayment(PaymentVerifyRequest request) {
        if (!verifySignature(request)) {
            throw new PaymentException("Payment verification failed! Invalid signature.");
        }

        Booking booking = bookingRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for this order!"));

        booking.setRazorpayPaymentId(request.getRazorpayPaymentId());
        booking.setRazorpaySignature(request.getRazorpaySignature());
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        // Send confirmation email asynchronously
        emailService.sendBookingConfirmationEmail(booking);

        return BookingResponse.from(booking);
    }

    // ✅ HMAC-SHA256 signature verification
    // Razorpay generates: HMAC_SHA256(orderId + "|" + paymentId, secretKey)
    // We generate same and compare - if they match, payment is genuine
    private boolean verifySignature(PaymentVerifyRequest request) {
        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(request.getRazorpaySignature());

        } catch (Exception e) {
            throw new PaymentException("Signature verification error: " + e.getMessage());
        }
    }

}
