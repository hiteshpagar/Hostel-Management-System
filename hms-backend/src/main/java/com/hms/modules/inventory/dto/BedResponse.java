package com.hms.modules.inventory.dto;

import com.hms.common.enums.BedStatus;

import java.time.LocalDateTime;

public class BedResponse {

    private Long id;
    private Long hostelId;
    private Long roomId;
    private String roomNumber;
    private String bedLabel;
    private BedStatus status;
    private LocalDateTime createdAt;

    public BedResponse() {
    }

    public BedResponse(Long id, Long hostelId, Long roomId, String roomNumber,
                       String bedLabel, BedStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.hostelId = hostelId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.bedLabel = bedLabel;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHostelId() {
        return hostelId;
    }

    public void setHostelId(Long hostelId) {
        this.hostelId = hostelId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBedLabel() {
        return bedLabel;
    }

    public void setBedLabel(String bedLabel) {
        this.bedLabel = bedLabel;
    }

    public BedStatus getStatus() {
        return status;
    }

    public void setStatus(BedStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
