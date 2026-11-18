package com.rubp.whattoeat.server.feature.account.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class AccountExceptionHandler {

    @ExceptionHandler(AccountUnavailableException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnavailableAccount(){
        return new ErrorResponse(
                "ACCOUNT_UNAVAILABLE",
                "当前账号不可用"
        );
    }

    @ExceptionHandler(InvalidInvitationCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidInvitationCode() {
        return new ErrorResponse(
                "INVALID_INVITATION_CODE",
                "邀请码无效、已过期或已使用"
        );
    }
}
