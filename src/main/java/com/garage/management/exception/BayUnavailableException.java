package com.garage.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class BayUnavailableException extends RuntimeException {
    public BayUnavailableException(String message) {
        super(message);
    }
}
