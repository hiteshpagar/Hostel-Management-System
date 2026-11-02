package com.hms.modules.leave.dto;

import com.hms.common.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewLeaveRequest {

    @NotNull(message = "Decision status is required (APPROVED or REJECTED)")
    private LeaveStatus status;

    @Size(max = 255, message = "Rejection reason cannot exceed 255 characters")
    private String rejectionReason;

    public ReviewLeaveRequest() {
    }

    public ReviewLeaveRequest(LeaveStatus status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
