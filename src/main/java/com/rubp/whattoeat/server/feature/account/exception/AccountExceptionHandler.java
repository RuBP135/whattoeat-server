package com.rubp.whattoeat.server.feature.account.exception;

import com.rubp.whattoeat.server.core.web.error.ErrorResponse;
import com.rubp.whattoeat.server.feature.invitation.exception.InvalidInvitationCodeException;
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

    @ExceptionHandler(EmailCooldownException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse handleEmailCooldown() {
        return new ErrorResponse(
                "EMAIL_COOLDOWN",
                "验证码发送过于频繁，请稍后再试"
        );
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidVerificationCode() {
        return new ErrorResponse(
                "INVALID_VERIFICATION_CODE",
                "验证码无效或已过期"
        );
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyRegistered() {
        return new ErrorResponse(
                "EMAIL_ALREADY_REGISTERED",
                "该邮箱已注册"
        );
    }
}
