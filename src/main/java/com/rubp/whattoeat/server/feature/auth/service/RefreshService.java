package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.model.RefreshAccessTokenRequest;
import com.rubp.whattoeat.server.feature.auth.model.RefreshAccessTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class RefreshService {

    private final JwtDecoder jwtRefreshDecoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RefreshService(
            @Qualifier("jwtRefreshDecoder") JwtDecoder jwtRefreshDecoder,
            JwtTokenService jwtTokenService,
            RefreshTokenRepository refreshTokenRepository,
            Clock clock
    ) {
        this.jwtRefreshDecoder = jwtRefreshDecoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }


    @Transactional
    public RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest request) {
        String oldRefreshToken = request.refreshToken();
        Jwt oldRefreshJwt = jwtRefreshDecoder.decode(oldRefreshToken);

        String oldHash = jwtTokenService.hashRefreshToken(oldRefreshToken);
        RefreshTokenEntry entry = refreshTokenRepository.findByRefreshTokenHash(oldHash)
                .orElseThrow(() -> new JwtException("refresh token 无效或已过期"));

        AccountEntry account = entry.getAccount();
        long id = account.getId();
        if (!Long.toString(id).equals(oldRefreshJwt.getSubject())) {
            throw new JwtException("refresh token 的账户 id 与记录不匹配");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountUnavailableException();
        }

        Instant expiresAt = Objects.requireNonNull(oldRefreshJwt.getExpiresAt(), "解码后的 refresh token 必须包含 exp");
        String newRefreshToken = jwtTokenService.createRefreshToken(id, expiresAt);
        String accessToken = jwtTokenService.createToken(id, JwtTokenType.ACCESS);
        String newHash = jwtTokenService.hashRefreshToken(newRefreshToken);

        Instant now = Instant.now(clock);
        if (!expiresAt.isAfter(now) || refreshTokenRepository.replaceRefreshTokenHash(oldHash, newHash, now) != 1) {
            throw new JwtException("refresh token 无效或已过期");
        }

        return new RefreshAccessTokenResponse(accessToken, newRefreshToken);
    }
}
