package com.carrental.service;

import com.carrental.dto.response.AdminDashboardResponse;
import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired private CarRepository carRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    // ✅ Admin dashboard - all stats in one call
    public AdminDashboardResponse getDashboardStats() {
        return AdminDashboardResponse.builder()
                .totalCars(carRepository.count())
                .availableCars(carRepository.findByStatus(Car.CarStatus.AVAILABLE).size())
                .bookedCars(carRepository.findByStatus(Car.CarStatus.BOOKED).size())
                .maintenanceCars(carRepository.findByStatus(Car.CarStatus.MAINTENANCE).size())

                .totalUsers(userRepository.count())
                .totalBookings(bookingRepository.count())
                .confirmedBookings(bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED))
                .pendingBookings(bookingRepository.countByStatus(Booking.BookingStatus.PENDING))
                .cancelledBookings(bookingRepository.countByStatus(Booking.BookingStatus.CANCELLED))
                .completedBookings(bookingRepository.countByStatus(Booking.BookingStatus.COMPLETED))

                .totalRevenue(bookingRepository.getTotalRevenue())
                .totalPaidPayments(bookingRepository.countByPaymentStatus(Booking.PaymentStatus.PAID))
                .build();
    }
}