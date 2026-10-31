package com.hms.modules.student.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TransferBedRequest {

    @NotNull(message = "New Bed ID is required")
    private Long newBedId;

    private LocalDate transferDate;
    private String notes;

    public TransferBedRequest() {
    }

    public Long getNewBedId() {
        return newBedId;
    }

    public void setNewBedId(Long newBedId) {
        this.newBedId = newBedId;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
