package com.pgManagement.roomservice.controller;

import com.pgManagement.roomservice.dto.CreateRoomRequest;
import com.pgManagement.roomservice.dto.CreateRoomResponse;
import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.entity.RoomType;
import com.pgManagement.roomservice.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<CreateRoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<CreateRoomResponse> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.getRoomDetails(roomId));
    }

    // GET /api/v1/rooms?pgId=&status=&type=   (status and type are optional)
    @GetMapping
    public ResponseEntity<List<CreateRoomResponse>> getRooms(
            @RequestParam UUID pgId,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) RoomType type) {
        return ResponseEntity.ok(roomService.getRoomsByCriteria(pgId, status, type));
    }

    @PutMapping("/{roomId}/status")
    public ResponseEntity<CreateRoomResponse> updateRoomStatus(
            @PathVariable UUID roomId,
            @RequestParam RoomStatus newStatus) {
        return ResponseEntity.ok(roomService.updateRoomStatus(roomId, newStatus));
    }

    @PutMapping("/{roomId}/type")
    public ResponseEntity<CreateRoomResponse> updateRoomType(
            @PathVariable UUID roomId,
            @RequestParam RoomType newType) {
        return ResponseEntity.ok(roomService.updateRoomType(roomId, newType));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<CreateRoomResponse> updateRoomDetails(
            @PathVariable UUID roomId,
            @RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoomDetails(roomId, request));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
