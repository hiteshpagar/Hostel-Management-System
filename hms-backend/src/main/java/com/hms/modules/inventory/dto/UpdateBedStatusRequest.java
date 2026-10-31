package com.hms.modules.inventory.dto;

import com.hms.common.enums.BedStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateBedStatusRequest {

    @NotNull(message = "Bed status is required (AVAILABLE, OCCUPIED, MAINTENANCE)")
    private BedStatus status;

    public UpdateBedStatusRequest() {
    }

    public BedStatus getStatus() {
        return status;
    }

    public void setStatus(BedStatus status) {
        this.status = status;
    }
}
