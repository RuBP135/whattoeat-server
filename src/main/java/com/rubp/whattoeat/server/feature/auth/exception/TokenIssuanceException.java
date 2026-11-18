package com.rubp.whattoeat.server.feature.auth.exception;

public class TokenIssuanceException extends RuntimeException {
    public TokenIssuanceException(Throwable cause) {
        super("Failed to issue token", cause);
    }
}
