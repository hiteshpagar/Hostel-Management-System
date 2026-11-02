package com.hms.modules.leave.dto;

import com.hms.common.enums.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ApplyLeaveRequest {

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Expected return time is required")
    private LocalDateTime expectedReturnTime;

    @NotBlank(message = "Reason for leave is required")
    @Size(min = 5, max = 1000, message = "Reason must be between 5 and 1000 characters")
    private String reason;

    private String emergencyContact;

    public ApplyLeaveRequest() {
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
