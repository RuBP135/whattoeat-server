package com.rubp.whattoeat.server.feature.invitation.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import com.rubp.whattoeat.server.core.web.error.ErrorResponseFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class InvitationExceptionHandler {

    private final ErrorResponseFactory errorResponseFactory;

    public InvitationExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(InvalidInvitationCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidInvitationCode() {
        return errorResponseFactory.create(
                "INVALID_INVITATION_CODE",
                "invitation.invalid_code"
        );
    }
}
