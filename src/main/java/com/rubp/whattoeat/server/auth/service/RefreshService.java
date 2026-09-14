package com.rubp.whattoeat.server.auth.service;

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

    public RefreshService(
            @Qualifier("jwtRefreshDecoder") JwtDecoder jwtRefreshDecoder,
            JwtTokenService jwtTokenService
    ) {
        this.jwtRefreshDecoder = jwtRefreshDecoder;
        this.jwtTokenService = jwtTokenService;
    }


    public RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest request){
        Jwt refreshToken = jwtRefreshDecoder.decode(request.refreshToken());

        String uid = refreshToken.getSubject();

        String accessToken = jwtTokenService.createToken(uid, JwtTokenType.ACCESS);

        return new RefreshAccessTokenResponse(accessToken);
    }
}
