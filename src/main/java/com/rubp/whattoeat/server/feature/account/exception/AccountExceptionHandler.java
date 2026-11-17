package com.rubp.whattoeat.server.feature.account.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountExceptionHandler {

    @ExceptionHandler(AccountUnavailableException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnavailableAccount(){
        return new ErrorResponse(
                "ACCOUNT_UNAVAILABLE",
                "当前账号不可用"
        );
    }
}
