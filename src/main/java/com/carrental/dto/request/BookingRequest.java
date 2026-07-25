package com.carrental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotBlank(message = "Start date is required (yyyy-MM-dd)")
    private String startDate;

    @NotBlank(message = "End date is required (yyyy-MM-dd)")
    private String endDate;
}
