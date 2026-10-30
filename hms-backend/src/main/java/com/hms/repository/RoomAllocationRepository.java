package com.hms.repository;

import com.hms.common.enums.AllocationStatus;
import com.hms.entity.RoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {
    Optional<RoomAllocation> findByStudentIdAndStatus(Long studentId, AllocationStatus status);
    Optional<RoomAllocation> findByBedIdAndStatus(Long bedId, AllocationStatus status);
    List<RoomAllocation> findByHostelIdAndStatus(Long hostelId, AllocationStatus status);
    List<RoomAllocation> findByStudentId(Long studentId);
}
