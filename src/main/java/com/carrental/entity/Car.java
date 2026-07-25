package com.carrental.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;           // Toyota

    @Column(nullable = false)
    private String model;           // Innova

    @Column(nullable = false)
    private String color;

    @Column(nullable = false, unique = true)
    private String licensePlate;    // MH12AB1234

    @Column(nullable = false)
    private int year;               // 2022

    @Column(nullable = false)
    private int seatingCapacity;

    @Column(nullable = false)
    private double pricePerDay;     // in INR

    @Column(nullable = false)
    private String fuelType;        // Petrol / Diesel / Electric / CNG

    @Column(nullable = false)
    private String transmission;    // Manual / Automatic

    @Column(nullable = false)
    private String category;        // Economy / SUV / Luxury / Sedan

    @Column(length = 500)
    private String imageUrl;        // car image link

    @Column(nullable = false)
    private double mileage;         // km per litre

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status = CarStatus.AVAILABLE;

    // OPTIMISTIC LOCKING
    // Prevents double booking when two users try simultaneously
    @Version
    private int version;

    @Column(nullable = false, updatable = false)
    private LocalDate addedAt = LocalDate.now();

    @JsonIgnore
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    public enum CarStatus {
        AVAILABLE, BOOKED, MAINTENANCE, DELETED
    }
}