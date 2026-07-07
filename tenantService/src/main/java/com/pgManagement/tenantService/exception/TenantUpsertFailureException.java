package com.pgManagement.tenantService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TenantUpsertFailureException extends RuntimeException {
    public TenantUpsertFailureException(String message) {
        super(message);
    }
}
