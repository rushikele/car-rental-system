package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// ✅ Razorpay sends these 3 fields after payment
// We verify signature to confirm payment is genuine
@Data
public class PaymentVerifyRequest {

    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}
