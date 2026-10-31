package com.hms.modules.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class UpdateRoomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 30)
    private String roomNumber;

    @NotNull(message = "Floor is required")
    @Min(0)
    private Integer floor;

    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.00")
    private BigDecimal monthlyRent;

    public UpdateRoomRequest() {
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
}
