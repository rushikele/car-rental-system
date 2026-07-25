package com.carrental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ✅ Returned after creating Razorpay order
// Frontend uses this to open Razorpay payment popup

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {
    private String razorpayOrderId;
    private double amount;          // in INR
    private String currency;        // INR
    private Long bookingId;
    private String keyId;           // Razorpay key_id for frontend
}
