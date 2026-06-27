package com.az.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class IdentityProviderException extends RuntimeException {

    public IdentityProviderException(String message) {
        super(message);
    }

    public IdentityProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
