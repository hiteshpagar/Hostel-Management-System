package com.hms.repository;

import com.hms.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, Long> {
    Optional<Hostel> findByCode(String code);
    boolean existsByCode(String code);
}
