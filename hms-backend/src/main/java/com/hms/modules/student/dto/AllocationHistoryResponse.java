package com.hms.modules.student.dto;

import com.hms.common.enums.AllocationStatus;
import java.time.LocalDate;

public class AllocationHistoryResponse {

    private Long allocationId;
    private Long bedId;
    private String bedLabel;
    private String roomNumber;
    private String blockName;
    private LocalDate startDate;
    private LocalDate endDate;
    private AllocationStatus status;
    private String notes;

    public AllocationHistoryResponse() {
    }

    public AllocationHistoryResponse(Long allocationId, Long bedId, String bedLabel, String roomNumber,
                                     String blockName, LocalDate startDate, LocalDate endDate,
                                     AllocationStatus status, String notes) {
        this.allocationId = allocationId;
        this.bedId = bedId;
        this.bedLabel = bedLabel;
        this.roomNumber = roomNumber;
        this.blockName = blockName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.notes = notes;
    }

    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public String getBedLabel() {
        return bedLabel;
    }

    public void setBedLabel(String bedLabel) {
        this.bedLabel = bedLabel;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public AllocationStatus getStatus() {
        return status;
    }

    public void setStatus(AllocationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
