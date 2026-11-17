package com.rubp.whattoeat.server.feature.auth.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(TokenIssuanceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleTokenIssuanceFailure(TokenIssuanceException exception) {
        log.error("token 签发失败", exception);
        return new ErrorResponse(
                "TOKEN_ISSUANCE_FAILED",
                "token 签发失败"
        );
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidJwt() {
        return new ErrorResponse(
                "INVALID_REFRESH_TOKEN",
                "refresh token 无效或已过期"
        );
    }

}
