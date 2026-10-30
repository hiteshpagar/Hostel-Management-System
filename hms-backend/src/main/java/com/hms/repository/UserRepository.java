package com.hms.repository;

import com.hms.common.enums.RoleType;
import com.hms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByHostelId(Long hostelId);
    List<User> findByHostelIdAndRole(Long hostelId, RoleType role);
}
