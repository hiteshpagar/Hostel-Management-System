package com.hms.repository;

import com.hms.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);
    Optional<StudentProfile> findByHostelIdAndAdmissionNumber(Long hostelId, String admissionNumber);
    List<StudentProfile> findByHostelId(Long hostelId);
    boolean existsByHostelIdAndAdmissionNumber(Long hostelId, String admissionNumber);
}
