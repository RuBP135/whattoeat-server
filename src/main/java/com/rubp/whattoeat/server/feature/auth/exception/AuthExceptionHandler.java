package com.rubp.whattoeat.server.feature.auth.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import com.rubp.whattoeat.server.core.web.error.ErrorResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    private final ErrorResponseFactory errorResponseFactory;

    public AuthExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(TokenIssuanceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleTokenIssuanceFailure(TokenIssuanceException exception) {
        log.error("Failed to issue token", exception);
        return errorResponseFactory.create(
                "TOKEN_ISSUANCE_FAILED",
                "auth.token_issuance_failed"
        );
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidJwt() {
        return errorResponseFactory.create(
                "INVALID_REFRESH_TOKEN",
                "auth.invalid_refresh_token"
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentials() {
        return errorResponseFactory.create(
                "INVALID_CREDENTIALS",
                "auth.invalid_credentials"
        );
    }
}
