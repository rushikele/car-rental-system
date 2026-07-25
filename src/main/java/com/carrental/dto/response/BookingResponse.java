package com.carrental.dto.response;

import com.carrental.entity.Booking;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ✅ INVOICE-STYLE BOOKING RESPONSE
// Shows full breakdown: days, base amount, GST, final amount

@Data
public class BookingResponse {

    private Long bookingId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String carBrand;
    private String carModel;
    private String carLicensePlate;
    private String carCategory;

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;

    private double pricePerDay;
    private double baseAmount;
    private double gstAmount;       // 18% GST
    private double finalAmount;

    private Booking.BookingStatus bookingStatus;
    private Booking.PaymentStatus paymentStatus;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private LocalDate bookedAt;

    // ✅ Factory method - converts Booking entity to this response
    public static BookingResponse from(Booking booking) {
        BookingResponse res = new BookingResponse();
        res.setBookingId(booking.getId());

        if (booking.getUser() != null) {
            res.setCustomerName(booking.getUser().getName());
            res.setCustomerEmail(booking.getUser().getEmail());
            res.setCustomerPhone(booking.getUser().getPhone());
        }

        if (booking.getCar() != null) {
            res.setCarBrand(booking.getCar().getBrand());
            res.setCarModel(booking.getCar().getModel());
            res.setCarLicensePlate(booking.getCar().getLicensePlate());
            res.setCarCategory(booking.getCar().getCategory());
            res.setPricePerDay(booking.getCar().getPricePerDay());
        }

        res.setStartDate(booking.getStartDate());
        res.setEndDate(booking.getEndDate());
        res.setTotalDays(booking.getTotalDays());
        res.setBaseAmount(booking.getTotalAmount());
        res.setGstAmount(booking.getTaxAmount());
        res.setFinalAmount(booking.getFinalAmount());
        res.setBookingStatus(booking.getStatus());
        res.setPaymentStatus(booking.getPaymentStatus());
        res.setRazorpayOrderId(booking.getRazorpayOrderId());
        res.setRazorpayPaymentId(booking.getRazorpayPaymentId());
        res.setBookedAt(booking.getBookedAt());

        return res;
    }
}
