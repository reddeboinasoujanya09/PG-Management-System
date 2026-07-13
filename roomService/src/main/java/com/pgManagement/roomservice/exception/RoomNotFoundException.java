package com.pgManagement.roomservice.exception;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(UUID roomId) {
        super("Room not found with ID: " + roomId);
    }
}
