package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.domain.JwtTokenType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SessionTokenService {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public SessionTokenService(
            JwtTokenService jwtTokenService,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public TokenPair issueTokenPair(AccountEntry account) {
        long accountId = account.getId();
        Role role = account.getRole();

        String accessToken = jwtTokenService.createToken(accountId, role, JwtTokenType.ACCESS);
        Jwt refreshJwt = jwtTokenService.createJwt(accountId, role, JwtTokenType.REFRESH);
        String refreshToken = refreshJwt.getTokenValue();

        String refreshTokenHash = jwtTokenService.hashRefreshToken(refreshToken);
        Instant expiresAt = refreshJwt.getExpiresAt();
        refreshTokenRepository.save(new RefreshTokenEntry(account, refreshTokenHash, expiresAt));

        return new TokenPair(accessToken, refreshToken);
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}
