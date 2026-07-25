package com.carrental.dto.response;

import lombok.Builder;
import lombok.Data;

// ✅ ADMIN DASHBOARD STATS RESPONSE

@Data
@Builder
public class AdminDashboardResponse {
    private long totalCars;
    private long availableCars;
    private long bookedCars;
    private long maintenanceCars;

    private long totalUsers;
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private long cancelledBookings;
    private long completedBookings;

    private double totalRevenue;
    private long totalPaidPayments;
}
