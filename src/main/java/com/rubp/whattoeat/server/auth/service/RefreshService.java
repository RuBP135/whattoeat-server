package com.rubp.whattoeat.server.auth.service;

import com.rubp.whattoeat.server.account.AccountEntry;
import com.rubp.whattoeat.server.account.AccountRepository;
import com.rubp.whattoeat.server.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.auth.model.RefreshAccessTokenRequest;
import com.rubp.whattoeat.server.auth.model.RefreshAccessTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class RefreshService {

    private final JwtDecoder jwtRefreshDecoder;
    private final JwtTokenService jwtTokenService;
    private final AccountRepository accountRepository;

    public RefreshService(
            @Qualifier("jwtRefreshDecoder") JwtDecoder jwtRefreshDecoder,
            JwtTokenService jwtTokenService,
            AccountRepository accountRepository
    ) {
        this.jwtRefreshDecoder = jwtRefreshDecoder;
        this.jwtTokenService = jwtTokenService;
        this.accountRepository = accountRepository;
    }


    public RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest request){
        Jwt refreshToken = jwtRefreshDecoder.decode(request.refreshToken());

        String uid = refreshToken.getSubject();

        AccountEntry account = accountRepository.findByUid(uid)
                .orElseThrow(AccountUnavailableException::new);

        if(account.getStatus() == AccountStatus.DISABLED){
            throw new AccountUnavailableException();
        }

        String accessToken = jwtTokenService.createToken(uid, JwtTokenType.ACCESS);

        return new RefreshAccessTokenResponse(accessToken);
    }
}
