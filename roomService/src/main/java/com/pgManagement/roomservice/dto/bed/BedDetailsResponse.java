package com.pgManagement.roomservice.dto.bed;

import com.pgManagement.roomservice.entity.AssignmentStatus;
import com.pgManagement.roomservice.entity.BedStatus;
import java.util.UUID;

public record BedDetailsResponse(
        String bedId,
        String bedLabel,
        BedStatus bedStatus,
        UUID roomId,
        UUID pgId,
        UUID currentAssignmentId,
        String currentTenantId,
        AssignmentStatus currentAssignmentStatus) {}
