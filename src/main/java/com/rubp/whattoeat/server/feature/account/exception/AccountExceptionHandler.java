package com.rubp.whattoeat.server.feature.account.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import com.rubp.whattoeat.server.core.web.error.ErrorResponseFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class AccountExceptionHandler {

    private final ErrorResponseFactory errorResponseFactory;

    public AccountExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(AccountUnavailableException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnavailableAccount() {
        return errorResponseFactory.create(
                "ACCOUNT_UNAVAILABLE",
                "account.unavailable"
        );
    }

    @ExceptionHandler(EmailCooldownException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse handleEmailCooldown() {
        return errorResponseFactory.create(
                "EMAIL_COOLDOWN",
                "account.email_cooldown"
        );
    }

    @ExceptionHandler(InvalidRegistrationSessionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRegistrationSession() {
        return errorResponseFactory.create(
                "INVALID_REGISTRATION_SESSION",
                "account.invalid_registration_session"
        );
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidVerificationCode() {
        return errorResponseFactory.create(
                "INVALID_VERIFICATION_CODE",
                "account.invalid_verification_code"
        );
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyRegistered() {
        return errorResponseFactory.create(
                "EMAIL_ALREADY_REGISTERED",
                "account.email_already_registered"
        );
    }
}
