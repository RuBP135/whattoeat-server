package com.rubp.whattoeat.server.auth.exception;

import com.rubp.whattoeat.server.web.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidJwt() {
        return new ErrorResponse(
                "INVALID_REFRESH_TOKEN",
                "refresh token 无效或已过期"
        );
    }

}
