package com.pgManagement.roomservice.service;

import com.pgManagement.roomservice.dto.CreateRoomRequest;
import com.pgManagement.roomservice.dto.CreateRoomResponse;
import com.pgManagement.roomservice.dto.CreateRoomResponse.BedResponse;
import com.pgManagement.roomservice.entity.Bed;
import com.pgManagement.roomservice.entity.BedStatus;
import com.pgManagement.roomservice.entity.AssignmentStatus;
import com.pgManagement.roomservice.entity.Room;
import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.entity.RoomType;
import com.pgManagement.roomservice.entity.TenantType;
import com.pgManagement.roomservice.exception.DuplicateRoomException;
import com.pgManagement.roomservice.exception.RoomNotFoundException;
import com.pgManagement.roomservice.repository.RoomAssignmentRepository;
import com.pgManagement.roomservice.repository.RoomRepository;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private static final Set<AssignmentStatus> OPEN_ASSIGNMENT_STATUSES =
            EnumSet.of(AssignmentStatus.PENDING_VERIFICATION, AssignmentStatus.ACTIVE);

    private final RoomRepository roomRepository;
    private final RoomAssignmentRepository assignmentRepository;

    RoomService(RoomRepository roomRepository, RoomAssignmentRepository assignmentRepository) {
        this.roomRepository = roomRepository;
        this.assignmentRepository = assignmentRepository;
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
    public List<CreateRoomResponse> getRoomsByCriteria(
            UUID pgId, RoomStatus status, RoomType type, Short floor, Integer minAvailableBeds, TenantType tenantType) {

        List<Room> rooms = roomRepository.findByPgIdAndFilters(pgId, status, type, floor);

        return rooms.stream()
                .filter(r -> minAvailableBeds == null || availableBeds(r) >= minAvailableBeds)
                .filter(r -> tenantType == null || isTenantTypeCompatible(r, tenantType))
                .map(this::toResponse)
                .toList();
    }

    private long availableBeds(Room room) {
        return room.getBeds().stream()
                .filter(b -> b.getBedStatus() == BedStatus.AVAILABLE)
                .count();
    }

    private boolean isTenantTypeCompatible(Room room, TenantType tenantType) {
        // Basic practical rule: room must have vacancy and not be maintenance.
        if (room.getRoomStatus() == RoomStatus.MAINTENANCE) return false;
        if (availableBeds(room) <= 0) return false;

        // Optional compatibility preference by current occupancy mix.
        if (tenantType == TenantType.PERMANENT) {
            return room.getRoomStatus() != RoomStatus.BOOKED_TEMP;
        }
        return room.getRoomStatus() != RoomStatus.BOOKED;
    }

    private void ensureRoomHasNoOpenAssignments(UUID roomId) {
        boolean hasOpen = assignmentRepository.existsByBed_Room_RoomIdAndStatusIn(roomId, OPEN_ASSIGNMENT_STATUSES);
        if (hasOpen) {
            throw new IllegalStateException("Cannot change room type/details while active or pending assignments exist.");
        }
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

        ensureRoomHasNoOpenAssignments(roomId);
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
            if (!room.getRoomNumber().equals(request.getRoomNumber())) {
                throw new IllegalStateException("Room number cannot be changed after creation.");
            }
            room.setRoomNumber(request.getRoomNumber());
        }
        if (request.getFloor() != null) {
            room.setFloor(request.getFloor());
        }
        if (request.getRoomType() != null) {
            ensureRoomHasNoOpenAssignments(roomId);
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
