package com.carrental.repository;

import com.carrental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByCarId(Long carId);

    Optional<Booking> findByRazorpayOrderId(String orderId);

    // ✅ ADMIN STATS - Total revenue from paid bookings
    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b WHERE b.paymentStatus = 'PAID'")
    Double getTotalRevenue();

    // Count bookings by status
    long countByStatus(Booking.BookingStatus status);

    // Count paid payments
    long countByPaymentStatus(Booking.PaymentStatus paymentStatus);

    // Monthly revenue
    @Query(value = """
        SELECT MONTH(booked_at) as month, SUM(final_amount) as revenue
        FROM bookings WHERE YEAR(booked_at) = :year AND payment_status = 'PAID'
        GROUP BY MONTH(booked_at) ORDER BY month
    """, nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("year") int year);
}
