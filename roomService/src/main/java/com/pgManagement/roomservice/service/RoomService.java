package com.pgManagement.roomservice.service;

import com.pgManagement.roomservice.dto.CreateRoomRequest;
import com.pgManagement.roomservice.dto.CreateRoomResponse;
import com.pgManagement.roomservice.dto.CreateRoomResponse.BedResponse;
import com.pgManagement.roomservice.entity.Bed;
import com.pgManagement.roomservice.entity.BedStatus;
import com.pgManagement.roomservice.entity.Room;
import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.entity.RoomType;
import com.pgManagement.roomservice.exception.DuplicateRoomException;
import com.pgManagement.roomservice.exception.RoomNotFoundException;
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
            bed.setBedLabel(roomNumber + "-" + i);
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
        if (room.getAmenities() == null)
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

    @Transactional(readOnly = true)
    public CreateRoomResponse getRoomDetails(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public List<CreateRoomResponse> getRoomsByCriteria(UUID pgId, RoomStatus status, RoomType type) {
        List<Room> rooms = roomRepository.findByPgIdAndFilters(pgId, status, type);
        return rooms.stream().map(this::toResponse).toList();
    }

    @Transactional
    public CreateRoomResponse updateRoomStatus(UUID roomId, RoomStatus newStatus) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        room.setRoomStatus(newStatus);
        return toResponse(room);
    }

    @Transactional
    public CreateRoomResponse updateRoomType(UUID roomId, RoomType newType) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (room.getRoomType() == newType)
            throw new IllegalArgumentException("Room type is already " + newType);

        room.getBeds().clear();
        room.getBeds().addAll(generateBeds(room.getRoomNumber(), newType.getBedCount(), room));
        room.setRoomType(newType);
        return toResponse(room);
    }

    @Transactional
    public CreateRoomResponse updateRoomDetails(UUID roomId, CreateRoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (request.getRoomNumber() != null && !request.getRoomNumber().isBlank()) {
            if (!room.getRoomNumber().equals(request.getRoomNumber()) &&
                    roomRepository.existsByPgIdAndRoomNumber(room.getPgId(), request.getRoomNumber())) {
                throw new DuplicateRoomException(request.getRoomNumber());
            }
            room.setRoomNumber(request.getRoomNumber());
        }
        if (request.getFloor() != null) {
            room.setFloor(request.getFloor());
        }
        if (request.getRoomType() != null) {
            room.getBeds().clear();
            room.getBeds().addAll(generateBeds(room.getRoomNumber(), request.getRoomType().getBedCount(), room));
            room.setRoomType(request.getRoomType());
        }
        if (request.getMonthlyRent() != null) {
            room.setMonthlyRent(request.getMonthlyRent());
        }
        if (request.getAmenities() != null) {
            room.setAmenities(request.getAmenities());
        }
        return toResponse(room);
    }

    @Transactional
    public void deleteRoom(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        for (Bed bed : room.getBeds()) {
            if (bed.getBedStatus() != BedStatus.AVAILABLE && bed.getBedStatus() != BedStatus.MAINTENANCE) {
                throw new IllegalStateException("Cannot delete room with occupied beds.");
            }
        }
        roomRepository.deleteById(roomId);
    }



}
