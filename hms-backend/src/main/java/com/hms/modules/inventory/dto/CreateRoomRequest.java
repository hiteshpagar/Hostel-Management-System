package com.hms.modules.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateRoomRequest {

    @NotBlank(message = "Room number is required (e.g. 101, 204A)")
    @Size(max = 30, message = "Room number must not exceed 30 characters")
    private String roomNumber;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floor = 0;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity cannot exceed 10")
    private Integer capacity = 1;

    @NotBlank(message = "Room type is required (e.g. AC, NON_AC)")
    private String roomType = "NON_AC";

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.00", message = "Monthly rent cannot be negative")
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    private boolean autoGenerateBeds = true;

    public CreateRoomRequest() {
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public boolean isAutoGenerateBeds() {
        return autoGenerateBeds;
    }

    public void setAutoGenerateBeds(boolean autoGenerateBeds) {
        this.autoGenerateBeds = autoGenerateBeds;
    }
}
