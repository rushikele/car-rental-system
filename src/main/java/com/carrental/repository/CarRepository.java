package com.carrental.repository;

import com.carrental.entity.Car;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByStatus(Car.CarStatus status);

    boolean existsByLicensePlate(String licensePlate);

    // ✅ PESSIMISTIC LOCK - used during booking to prevent race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdWithLock(@Param("id") Long id);

    // ✅ AVAILABILITY CHECK BY DATE RANGE
    // Returns cars that are NOT booked between given dates
    // A car is unavailable if any confirmed booking overlaps with requested dates
    @Query("""
        SELECT c FROM Car c WHERE c.status = 'AVAILABLE'
        AND c.id NOT IN (
            SELECT b.car.id FROM Booking b
            WHERE b.status IN ('CONFIRMED', 'PENDING')
            AND NOT (b.endDate <= :startDate OR b.startDate >= :endDate)
        )
    """)
    List<Car> findAvailableCarsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Search by brand or model
    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' AND " +
           "(LOWER(c.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.model) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Car> searchAvailableCars(@Param("keyword") String keyword);

    // Filter by category
    List<Car> findByStatusAndCategory(Car.CarStatus status, String category);
}
