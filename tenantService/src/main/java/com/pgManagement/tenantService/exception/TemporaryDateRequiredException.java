package com.pgManagement.tenantService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TemporaryDateRequiredException extends RuntimeException {

    public TemporaryDateRequiredException(String message) {
        super(message);
    }
}
