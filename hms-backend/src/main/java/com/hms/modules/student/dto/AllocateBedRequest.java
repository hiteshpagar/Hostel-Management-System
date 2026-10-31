package com.hms.modules.student.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AllocateBedRequest {

    @NotNull(message = "Bed ID is required")
    private Long bedId;

    private LocalDate startDate;
    private String notes;

    public AllocateBedRequest() {
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
