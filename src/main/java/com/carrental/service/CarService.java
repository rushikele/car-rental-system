package com.carrental.service;

import com.carrental.dto.request.CarRequest;
import com.carrental.entity.Car;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    // ✅ All available cars
    public List<Car> getAvailableCars() {
        return carRepository.findByStatus(Car.CarStatus.AVAILABLE);
    }

    // ✅ Available cars by date range - checks bookings overlap
    public List<Car> getAvailableCarsByDateRange(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        if (startDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Start date cannot be in the past!");
        if (!endDate.isAfter(startDate))
            throw new IllegalArgumentException("End date must be after start date!");

        return carRepository.findAvailableCarsByDateRange(startDate, endDate);
    }

    // ✅ Search by keyword
    public List<Car> searchCars(String keyword) {
        return carRepository.searchAvailableCars(keyword);
    }

    // ✅ Filter by category
    public List<Car> getCarsByCategory(String category) {
        return carRepository.findByStatusAndCategory(Car.CarStatus.AVAILABLE, category);
    }

    // ✅ All cars (admin)
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // ✅ Get by id
    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
    }

    // ✅ Admin: Add car
    public Car addCar(CarRequest request) {
        if (carRepository.existsByLicensePlate(request.getLicensePlate()))
            throw new IllegalArgumentException("Car with this license plate already exists!");

        Car car = mapRequestToCar(new Car(), request);
        car.setStatus(Car.CarStatus.AVAILABLE);
        return carRepository.save(car);
    }

    // ✅ Admin: Update car details
    public Car updateCar(Long id, CarRequest request) {
        Car car = getCarById(id);
        mapRequestToCar(car, request);
        return carRepository.save(car);
    }

    // ✅ Admin: Soft delete (marks as DELETED, doesn't remove from DB)
    // Real world - you never hard delete data, just mark it
    public String deleteCar(Long id) {
        Car car = getCarById(id);
        if (car.getStatus() == Car.CarStatus.BOOKED)
            throw new IllegalArgumentException("Cannot delete a currently booked car!");
        car.setStatus(Car.CarStatus.DELETED);
        carRepository.save(car);
        return "Car deleted successfully (marked as inactive)";
    }

    // ✅ Admin: Update car status (AVAILABLE / MAINTENANCE etc.)
    public Car updateCarStatus(Long id, String status) {
        Car car = getCarById(id);
        try {
            car.setStatus(Car.CarStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Use: AVAILABLE, BOOKED, MAINTENANCE");
        }
        return carRepository.save(car);
    }

    // Helper: map request fields to car entity
    private Car mapRequestToCar(Car car, CarRequest request) {
        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setColor(request.getColor());
        car.setLicensePlate(request.getLicensePlate());
        car.setYear(request.getYear());
        car.setSeatingCapacity(request.getSeatingCapacity());
        car.setPricePerDay(request.getPricePerDay());
        car.setFuelType(request.getFuelType());
        car.setTransmission(request.getTransmission());
        car.setCategory(request.getCategory());
        car.setImageUrl(request.getImageUrl());
        car.setMileage(request.getMileage());
        return car;
    }
}
