package com.hms.repository;

import com.hms.entity.HostelBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HostelBlockRepository extends JpaRepository<HostelBlock, Long> {
    List<HostelBlock> findByHostelId(Long hostelId);
    Optional<HostelBlock> findByHostelIdAndName(Long hostelId, String name);
}
