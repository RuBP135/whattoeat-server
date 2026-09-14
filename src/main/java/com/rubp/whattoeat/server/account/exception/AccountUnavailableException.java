package com.rubp.whattoeat.server.account.exception;

public class AccountUnavailableException extends RuntimeException {
    public AccountUnavailableException() {
        super("当前账号不可用");
    }
}
