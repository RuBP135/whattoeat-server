package com.rubp.whattoeat.server.feature.account.exception;

public class AccountUnavailableException extends RuntimeException {
    public AccountUnavailableException() {
        super("Account is unavailable");
    }
}
