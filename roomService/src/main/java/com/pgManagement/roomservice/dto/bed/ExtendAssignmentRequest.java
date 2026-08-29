package com.pgManagement.roomservice.dto.bed;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ExtendAssignmentRequest(@NotNull LocalDate newVacatingDate) {}

