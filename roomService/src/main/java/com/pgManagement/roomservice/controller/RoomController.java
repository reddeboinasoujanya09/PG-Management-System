package com.pgManagement.roomservice.controller;

import com.pgManagement.roomservice.entity.Room;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/rooms")
public class RoomController {
    @PostMapping("")
    public ResponseEntity<?> createRoom(@RequestBody Room room) {
        // Logic to create a room
        return ResponseEntity.ok("Room created successfully");
    }
}
}
