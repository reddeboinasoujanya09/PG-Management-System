package com.pgManagement.roomservice.dto;

import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.entity.RoomType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateRoomResponse {
    private UUID roomId;
    private UUID pgId;
    private String roomNumber;
    private Short floor;
    private RoomType roomType;
    private RoomStatus roomStatus;
    private BigDecimal monthlyRent;
    private List<String> amenities;
    private List<BedResponse> beds;

    @Data
    public static class BedResponse {
        private String bedId;
        private String bedLabel;
        private String bedStatus;
    }
}
