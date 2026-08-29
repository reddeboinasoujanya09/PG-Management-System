package com.pgManagement.roomservice.dto.bed;

import com.pgManagement.roomservice.entity.AssignmentStatus;
import com.pgManagement.roomservice.entity.TenantType;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResponse(
        UUID assignmentId,
        String bedId,
        String tenantId,
        TenantType tenantType,
        LocalDate joiningDate,
        LocalDate vacatingDate,
        boolean verified,
        AssignmentStatus status) {}
