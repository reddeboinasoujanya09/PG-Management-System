package com.pgManagement.roomservice.dto.bed;

import com.pgManagement.roomservice.entity.VacateRequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VacateRequestResponse(
        UUID requestId,
        String bedId,
        UUID assignmentId,
        String tenantId,
        LocalDate requestedVacateDate,
        VacateRequestStatus status,
        LocalDateTime createdAt,
        String description) {}
