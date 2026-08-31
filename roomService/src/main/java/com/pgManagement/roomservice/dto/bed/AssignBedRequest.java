package com.pgManagement.roomservice.dto.bed;

import com.pgManagement.roomservice.entity.TenantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssignBedRequest(
        @NotBlank String tenantId,
        @NotNull TenantType tenantType,
        LocalDate joiningDate,
        LocalDate vacatingDate,
        BigDecimal advancePaid,
        String assignedBy,
        String notes) {}
