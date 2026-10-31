package com.hms.modules.student.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentDetailResponse extends StudentSummaryResponse {

    private String guardianName;
    private String guardianPhone;
    private String guardianRelation;
    private String emergencyContact;
    private String permanentAddress;
    private String bloodGroup;

    // Current Allocation Details
    private Long allocationId;
    private Long bedId;
    private Long roomId;
    private Long blockId;
    private String roomType;
    private Integer floor;
    private BigDecimal monthlyRent;
    private LocalDate allocationStartDate;
    private String allocationNotes;
    private String allocatedByName;

    private List<AllocationHistoryResponse> allocationHistory = new ArrayList<>();

    public StudentDetailResponse() {
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public String getGuardianRelation() {
        return guardianRelation;
    }

    public void setGuardianRelation(String guardianRelation) {
        this.guardianRelation = guardianRelation;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
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

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public LocalDate getAllocationStartDate() {
        return allocationStartDate;
    }

    public void setAllocationStartDate(LocalDate allocationStartDate) {
        this.allocationStartDate = allocationStartDate;
    }

    public String getAllocationNotes() {
        return allocationNotes;
    }

    public void setAllocationNotes(String allocationNotes) {
        this.allocationNotes = allocationNotes;
    }

    public String getAllocatedByName() {
        return allocatedByName;
    }

    public void setAllocatedByName(String allocatedByName) {
        this.allocatedByName = allocatedByName;
    }

    public List<AllocationHistoryResponse> getAllocationHistory() {
        return allocationHistory;
    }

    public void setAllocationHistory(List<AllocationHistoryResponse> allocationHistory) {
        this.allocationHistory = allocationHistory;
    }
}
