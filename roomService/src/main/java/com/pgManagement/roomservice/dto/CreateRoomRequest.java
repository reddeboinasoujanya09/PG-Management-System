package com.pgManagement.roomservice.dto;

import com.pgManagement.roomservice.entity.RoomType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateRoomRequest {

    @NotNull(message = "PG ID is required")
    private UUID pgId;

    private String roomNumber;

    @NotNull(message = "Floor is required")
    private Short floor;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Monthly rent is required")
    private BigDecimal monthlyRent;

    private List<String> amenities;
}
