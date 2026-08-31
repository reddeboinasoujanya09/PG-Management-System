package com.pgManagement.roomservice.repository;

import com.pgManagement.roomservice.entity.VacateRequest;
import com.pgManagement.roomservice.entity.VacateRequestStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacateRequestRepository extends JpaRepository<VacateRequest, UUID> {

    boolean existsByAssignment_AssignmentIdAndStatusIn(
            UUID assignmentId, Collection<VacateRequestStatus> statuses);

    List<VacateRequest> findByStatusAndRequestedVacateDate(
            VacateRequestStatus status, LocalDate requestedVacateDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select vr from VacateRequest vr join fetch vr.bed b join fetch vr.assignment a where vr.requestId = :requestId")
    Optional<VacateRequest> findByIdForUpdate(@Param("requestId") UUID requestId);
}
