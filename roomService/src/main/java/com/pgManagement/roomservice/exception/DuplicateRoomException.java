package com.pgManagement.roomservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRoomException extends RuntimeException {

    public DuplicateRoomException(String message) {
        super(message);
    }
}
