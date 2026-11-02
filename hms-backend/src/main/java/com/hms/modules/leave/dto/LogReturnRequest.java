package com.hms.modules.leave.dto;

import java.time.LocalDateTime;

public class LogReturnRequest {

    private LocalDateTime actualReturnTime;

    private String notes;

    public LogReturnRequest() {
    }

    public LogReturnRequest(LocalDateTime actualReturnTime, String notes) {
        this.actualReturnTime = actualReturnTime;
        this.notes = notes;
    }

    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
