package com.pgManagement.roomservice.dto.bed;

import com.pgManagement.roomservice.entity.BedStatus;
import jakarta.validation.constraints.NotNull;

public record BedStatusUpdateRequest(@NotNull BedStatus newStatus) {}
