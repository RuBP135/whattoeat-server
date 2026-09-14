package com.rubp.whattoeat.server.auth.service;

import com.rubp.whattoeat.server.account.AccountEntry;
import com.rubp.whattoeat.server.account.AccountService;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.auth.model.CreateAnonymousAccountResponse;
import com.rubp.whattoeat.server.auth.model.JwtTokenType;
import org.springframework.stereotype.Service;

@Service
public class AnonymousAuthService {

    private final AccountService accountService;
    private final JwtTokenService jwtTokenService;

    public AnonymousAuthService(AccountService accountService, JwtTokenService jwtTokenService) {
        this.accountService = accountService;
        this.jwtTokenService = jwtTokenService;
    }


    public CreateAnonymousAccountResponse createAnonymousAccount(){
        AccountEntry account = accountService.createAnonymousAccount();
        return new CreateAnonymousAccountResponse(
                jwtTokenService.createToken(account.getUid(), JwtTokenType.ACCESS),
                jwtTokenService.createToken(account.getUid(), JwtTokenType.REFRESH),
                account.getUid()
        );
    }
}
