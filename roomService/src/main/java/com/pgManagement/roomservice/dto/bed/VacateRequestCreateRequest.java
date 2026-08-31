package com.pgManagement.roomservice.dto.bed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VacateRequestCreateRequest(
        @NotBlank String tenantId,
        @NotNull LocalDate requestedVacateDate,
        String description) {}
