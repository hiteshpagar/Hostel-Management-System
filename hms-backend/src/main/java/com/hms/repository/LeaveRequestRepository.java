package com.hms.repository;

import com.hms.common.enums.LeaveStatus;
import com.hms.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStudentId(Long studentId);
    List<LeaveRequest> findByHostelIdAndStatus(Long hostelId, LeaveStatus status);
}
