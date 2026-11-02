package com.hms.repository;

import com.hms.common.enums.LeaveStatus;
import com.hms.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<LeaveRequest> findByHostelIdOrderByCreatedAtDesc(Long hostelId);

    List<LeaveRequest> findByHostelIdAndStatusOrderByCreatedAtDesc(Long hostelId, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.student.id = :studentId " +
           "AND lr.status IN :statuses " +
           "AND lr.departureTime < :returnTime " +
           "AND lr.expectedReturnTime > :departureTime")
    List<LeaveRequest> findOverlappingLeaves(
            @Param("studentId") Long studentId,
            @Param("statuses") Collection<LeaveStatus> statuses,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("returnTime") LocalDateTime returnTime
    );
}
