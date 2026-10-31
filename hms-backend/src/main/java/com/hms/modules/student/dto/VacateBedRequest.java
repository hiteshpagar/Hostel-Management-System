package com.hms.modules.student.dto;

import java.time.LocalDate;

public class VacateBedRequest {

    private LocalDate vacateDate;
    private String notes;

    public VacateBedRequest() {
    }

    public LocalDate getVacateDate() {
        return vacateDate;
    }

    public void setVacateDate(LocalDate vacateDate) {
        this.vacateDate = vacateDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
