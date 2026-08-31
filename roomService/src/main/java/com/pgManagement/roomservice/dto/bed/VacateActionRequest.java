package com.pgManagement.roomservice.dto.bed;

import com.pgManagement.roomservice.entity.VacateRequestStatus;
import jakarta.validation.constraints.NotNull;

public record VacateActionRequest(@NotNull VacateRequestStatus action) {}
