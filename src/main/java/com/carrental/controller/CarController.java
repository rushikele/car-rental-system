package com.carrental.controller;

import com.carrental.dto.request.CarRequest;
import com.carrental.dto.response.ApiResponse;
import com.carrental.service.CarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = "*")
public class CarController {

    @Autowired
    private CarService carService;

    // Public
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<?>> getAvailableCars() {
        return ResponseEntity.ok(ApiResponse.success("Available cars", carService.getAvailableCars()));
    }

    // Public - check available cars for specific date range
    @GetMapping("/available-by-date")
    public ResponseEntity<ApiResponse<?>> getAvailableByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(ApiResponse.success("Cars available for selected dates",
                carService.getAvailableCarsByDateRange(startDate, endDate)));
    }

    // Public - search by brand or model keyword
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Search results", carService.searchCars(keyword)));
    }

    // Public - filter by category
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<?>> getByCategory(@RequestParam String category) {
        return ResponseEntity.ok(ApiResponse.success("Cars by category", carService.getCarsByCategory(category)));
    }

    // Auth required - get car details
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Car details", carService.getCarById(id)));
    }

    // Admin only
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllCars() {
        return ResponseEntity.ok(ApiResponse.success("All cars", carService.getAllCars()));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addCar(@Valid @RequestBody CarRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Car added successfully!", carService.addCar(request)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<?>> updateCar(@PathVariable Long id,
                                                     @Valid @RequestBody CarRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Car updated successfully!", carService.updateCar(id, request)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(carService.deleteCar(id)));
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id,
                                                        @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated!",
                carService.updateCarStatus(id, status)));
    }
}
