package com.pgManagement.roomservice.repository;

import com.pgManagement.roomservice.entity.Room;
import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.entity.RoomType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT MAX(r.roomNumber) FROM Room r WHERE r.pgId = :pgId AND r.floor = :floor")
    Optional<String> findMaxRoomNumberForFloor(@Param("pgId") UUID pgId, @Param("floor") Short floor);

    boolean existsByPgIdAndRoomNumber(UUID pgId, String roomNumber);

    @Query("SELECT r FROM Room r WHERE r.pgId = :pgId " +
           "AND (:status IS NULL OR r.roomStatus = :status) " +
           "AND (:type IS NULL OR r.roomType = :type)")
    List<Room> findByPgIdAndFilters(@Param("pgId") UUID pgId,
                                    @Param("status") RoomStatus status,
                                    @Param("type") RoomType type);
}
