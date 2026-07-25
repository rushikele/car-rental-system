package com.carrental.controller;

import com.carrental.dto.response.ApiResponse;
import com.carrental.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ✅ Admin dashboard - total revenue, bookings, cars stats
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<?>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                "Dashboard stats", adminService.getDashboardStats()));
    }
}
