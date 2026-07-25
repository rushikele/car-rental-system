package com.carrental.service;

import com.carrental.dto.request.BookingRequest;
import com.carrental.dto.response.BookingResponse;
import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.User;
import com.carrental.exception.CarNotAvailableException;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;

    // ✅ STEP 1 of booking: Create booking (payment pending)
    // Full concurrency protection:
    // - PESSIMISTIC_WRITE lock: only 1 transaction can proceed at a time
    // - @Version: optimistic lock as second layer
    // - @Transactional: atomic - all or nothing
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());

        validateDates(startDate, endDate);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ PESSIMISTIC LOCK - locks DB row during this transaction
        // Two users trying to book same car:
        // User A acquires lock → User B WAITS
        // User A confirms car is AVAILABLE → marks BOOKED → releases lock
        // User B acquires lock → sees BOOKED → throws CarNotAvailableException
        Car car;
        try {
            car = carRepository.findByIdWithLock(request.getCarId())
                    .orElseThrow(() -> new ResourceNotFoundException("Car not found!"));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CarNotAvailableException("Car was just booked by someone else. Please try again.");
        }

        if (car.getStatus() != Car.CarStatus.AVAILABLE)
            throw new CarNotAvailableException("Car is not available for booking!");

        // Calculate amounts
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
        double baseAmount = totalDays * car.getPricePerDay();
        double gstAmount = baseAmount * 0.18;   // 18% GST
        double finalAmount = baseAmount + gstAmount;

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setTotalDays(totalDays);
        booking.setTotalAmount(baseAmount);
        booking.setTaxAmount(gstAmount);
        booking.setFinalAmount(finalAmount);
        booking.setStatus(Booking.BookingStatus.PENDING); // pending until payment
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);

        // ✅ Mark car as BOOKED atomically in same transaction
        car.setStatus(Car.CarStatus.BOOKED);
        carRepository.save(car);

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.from(saved);
    }

    // Get logged-in user's bookings
    public List<BookingResponse> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    // Get booking by ID
    public BookingResponse getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));

        // Users can only see their own bookings
        if (!booking.getUser().getEmail().equals(userEmail))
            throw new IllegalArgumentException("Access denied! This booking doesn't belong to you.");

        return BookingResponse.from(booking);
    }

    // ✅ Cancel booking - frees up the car
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));

        if (!booking.getUser().getEmail().equals(userEmail))
            throw new IllegalArgumentException("You can only cancel your own bookings!");

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw new IllegalArgumentException("Booking is already cancelled!");

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED)
            throw new IllegalArgumentException("Cannot cancel a completed booking!");

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        // If paid → mark for refund
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        }

        // ✅ Free up the car
        booking.getCar().setStatus(Car.CarStatus.AVAILABLE);
        carRepository.save(booking.getCar());

        Booking saved = bookingRepository.save(booking);

        // Send cancellation email async
        emailService.sendCancellationEmail(saved);

        return BookingResponse.from(saved);
    }

    // Admin: Get all bookings
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    // Admin: Mark booking as completed (car returned)
    @Transactional
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED)
            throw new IllegalArgumentException("Only confirmed bookings can be marked as completed!");

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.getCar().setStatus(Car.CarStatus.AVAILABLE); // car is free again
        carRepository.save(booking.getCar());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    public void deleteBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));

        if (!booking.getUser().getEmail().equals(userEmail))
            throw new IllegalArgumentException("You can only remove your own bookings!");

        if (booking.getStatus() != Booking.BookingStatus.CANCELLED)
            throw new IllegalArgumentException("Only cancelled bookings can be removed!");

        bookingRepository.delete(booking);
    }
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Start date cannot be in the past!");
        if (!endDate.isAfter(startDate))
            throw new IllegalArgumentException("End date must be after start date!");
        if (ChronoUnit.DAYS.between(startDate, endDate) > 30)
            throw new IllegalArgumentException("Maximum booking duration is 30 days!");
    }
}
