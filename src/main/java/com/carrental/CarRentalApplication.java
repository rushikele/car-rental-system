package com.carrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan(basePackages = "com.carrental")
@EnableAsync // enables async email sending
public class CarRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarRentalApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  Car Rental System Started Successfully!");
        System.out.println("  API running at: http://localhost:8080");
        System.out.println("==========================================");
    }
}
