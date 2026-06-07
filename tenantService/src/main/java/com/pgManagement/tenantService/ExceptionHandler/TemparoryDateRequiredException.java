package com.pgManagement.tenantService.ExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TemparoryDateRequiredException extends RuntimeException {

    public TemparoryDateRequiredException(String message) {
        super(message);
    }
}
