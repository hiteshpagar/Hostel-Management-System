package com.hms.repository;

import com.hms.common.enums.BedStatus;
import com.hms.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomId(Long roomId);
    List<Bed> findByHostelIdAndStatus(Long hostelId, BedStatus status);
    Optional<Bed> findByRoomIdAndBedLabel(Long roomId, String bedLabel);
    long countByHostelIdAndStatus(Long hostelId, BedStatus status);
}
