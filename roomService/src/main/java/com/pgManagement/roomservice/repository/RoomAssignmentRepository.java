package com.pgManagement.roomservice.repository;

import com.pgManagement.roomservice.entity.AssignmentStatus;
import com.pgManagement.roomservice.entity.RoomAssignment;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, UUID> {

    boolean existsByBed_BedIdAndStatusIn(String bedId, Collection<AssignmentStatus> statuses);

    boolean existsByTenantIdAndStatusIn(String tenantId, Collection<AssignmentStatus> statuses);

    boolean existsByBed_Room_RoomIdAndStatusIn(UUID roomId, Collection<AssignmentStatus> statuses);

    boolean existsByBed_BedIdAndStatusInAndJoiningDateGreaterThanEqual(
            String bedId, Collection<AssignmentStatus> statuses, LocalDate joiningDate);

    Optional<RoomAssignment> findFirstByBed_BedIdAndStatusInOrderByJoiningDateDesc(
            String bedId, Collection<AssignmentStatus> statuses);

    List<RoomAssignment> findByBed_Room_RoomIdAndStatusIn(UUID roomId, Collection<AssignmentStatus> statuses);

    List<RoomAssignment> findByStatusAndVacatingDateLessThanEqual(AssignmentStatus status, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from RoomAssignment a join fetch a.bed b where a.assignmentId = :assignmentId")
    Optional<RoomAssignment> findByIdForUpdate(@Param("assignmentId") UUID assignmentId);
}
