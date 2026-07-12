package com.pgManagement.roomservice.service;

import com.pgManagement.roomservice.dto.CreateRoomRequest;
import com.pgManagement.roomservice.dto.CreateRoomResponse;
import com.pgManagement.roomservice.dto.CreateRoomResponse.BedResponse;
import com.pgManagement.roomservice.entity.Bed;
import com.pgManagement.roomservice.entity.BedStatus;
import com.pgManagement.roomservice.entity.Room;
import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.exception.DuplicateRoomException;
import com.pgManagement.roomservice.repository.RoomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public CreateRoomResponse createRoom(CreateRoomRequest request) {
        String roomNumber = request.getRoomNumber();

        if (roomNumber == null || roomNumber.isBlank()) {
            roomNumber = generateRoomNumber(request.getPgId(), request.getFloor());
        }

        if (roomRepository.existsByPgIdAndRoomNumber(request.getPgId(), roomNumber)) {
            throw new DuplicateRoomException(roomNumber);
        }

        Room room = new Room();
        room.setRoomId(UUID.randomUUID());
        room.setPgId(request.getPgId());
        room.setRoomNumber(roomNumber);
        room.setFloor(request.getFloor());
        room.setRoomType(request.getRoomType());
        room.setMonthlyRent(request.getMonthlyRent());
        room.setAmenities(request.getAmenities());
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setBeds(generateBeds(roomNumber, request.getRoomType().getBedCount(), room));

        Room savedRoom = roomRepository.save(room);
        return toResponse(savedRoom);
    }

    private List<Bed> generateBeds(String roomNumber, int bedCount, Room room) {
        List<Bed> beds = new ArrayList<>();
        for (int i = 1; i <= bedCount; i++) {
            Bed bed = new Bed();
            bed.setBedId(roomNumber + "-" + i);
            bed.setBedLabel("Bed " + i);
            bed.setBedStatus(BedStatus.AVAILABLE);
            bed.setRoom(room);
            beds.add(bed);
        }
        return beds;
    }

    private String generateRoomNumber(UUID pgId, Short floor) {
        return roomRepository.findMaxRoomNumberForFloor(pgId, floor)
                .map(max -> String.valueOf(Integer.parseInt(max) + 1))
                .orElse(String.valueOf(floor * 100 + 1));
    }

    private CreateRoomResponse toResponse(Room room) {
        CreateRoomResponse response = new CreateRoomResponse();
        response.setRoomId(room.getRoomId());
        response.setPgId(room.getPgId());
        response.setRoomNumber(room.getRoomNumber());
        response.setFloor(room.getFloor());
        response.setRoomType(room.getRoomType());
        response.setRoomStatus(room.getRoomStatus());
        response.setMonthlyRent(room.getMonthlyRent());
        if(room.getAmenities() == null)
            response.setAmenities(new ArrayList<>());
        else
            response.setAmenities(room.getAmenities());
        response.setBeds(room.getBeds().stream().map(bed -> {
            BedResponse br = new BedResponse();
            br.setBedId(bed.getBedId());
            br.setBedLabel(bed.getBedLabel());
            br.setBedStatus(bed.getBedStatus().name());
            return br;
        }).toList());
        return response;
    }
}
