package com.hms.repository;

import com.hms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHostelId(Long hostelId);
    List<Room> findByBlockId(Long blockId);
    Optional<Room> findByBlockIdAndRoomNumber(Long blockId, String roomNumber);
    boolean existsByBlockIdAndRoomNumber(Long blockId, String roomNumber);
}
