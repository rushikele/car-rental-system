package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CarRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Color is required")
    private String color;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @Positive(message = "Year must be valid")
    private int year;

    @Positive(message = "Seating capacity must be positive")
    private int seatingCapacity;

    @Positive(message = "Price per day must be positive")
    private double pricePerDay;

    @NotBlank(message = "Fuel type is required")
    private String fuelType;

    @NotBlank(message = "Transmission is required")
    private String transmission;

    @NotBlank(message = "Category is required")
    private String category; // Economy / SUV / Luxury / Sedan

    private String imageUrl;

    @Positive(message = "Mileage must be positive")
    private double mileage;
}
