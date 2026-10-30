package com.hms.repository;

import com.hms.common.enums.ComplaintStatus;
import com.hms.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudentId(Long studentId);
    List<Complaint> findByHostelIdAndStatus(Long hostelId, ComplaintStatus status);
    List<Complaint> findByAssignedToId(Long staffUserId);
}
