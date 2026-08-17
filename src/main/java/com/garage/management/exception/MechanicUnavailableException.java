package com.garage.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MechanicUnavailableException extends RuntimeException {
    public MechanicUnavailableException(String message) {
        super(message);
    }
}
