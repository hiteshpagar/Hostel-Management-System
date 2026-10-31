package com.hms.modules.inventory.dto;

import com.hms.common.enums.GenderType;

import java.time.LocalDateTime;

public class BlockResponse {

    private Long id;
    private Long hostelId;
    private String hostelName;
    private String name;
    private GenderType genderType;
    private Integer totalFloors;
    private int totalRooms;
    private LocalDateTime createdAt;

    public BlockResponse() {
    }

    public BlockResponse(Long id, Long hostelId, String hostelName, String name,
                         GenderType genderType, Integer totalFloors, int totalRooms, LocalDateTime createdAt) {
        this.id = id;
        this.hostelId = hostelId;
        this.hostelName = hostelName;
        this.name = name;
        this.genderType = genderType;
        this.totalFloors = totalFloors;
        this.totalRooms = totalRooms;
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

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenderType getGenderType() {
        return genderType;
    }

    public void setGenderType(GenderType genderType) {
        this.genderType = genderType;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
