package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.feature.account.domain.AccountStatus;
import com.rubp.whattoeat.server.feature.account.domain.Role;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.domain.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.model.RefreshTokenPairRequest;
import com.rubp.whattoeat.server.feature.auth.model.RefreshTokenPairResponse;
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
public class TokenRefreshService {

    private final JwtDecoder jwtRefreshDecoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public TokenRefreshService(
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
    public RefreshTokenPairResponse refreshTokenPair(RefreshTokenPairRequest request) {
        String oldRefreshToken = request.refreshToken();
        Jwt oldRefreshJwt = jwtRefreshDecoder.decode(oldRefreshToken);

        String oldHash = jwtTokenService.hashRefreshToken(oldRefreshToken);
        RefreshTokenEntry entry = refreshTokenRepository.findByRefreshTokenHash(oldHash)
                .orElseThrow(() -> new JwtException("Refresh token is invalid or has expired"));

        AccountEntry account = entry.getAccount();
        long id = account.getId();
        Role role = account.getRole();
        if (!Long.toString(id).equals(oldRefreshJwt.getSubject())) {
            throw new JwtException("Refresh token subject does not match the stored account ID");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountUnavailableException();
        }

        Instant expiresAt = Objects.requireNonNull(oldRefreshJwt.getExpiresAt(), "Decoded refresh token must contain the exp claim");
        String newRefreshToken = jwtTokenService.createRefreshToken(id, role, expiresAt);
        String accessToken = jwtTokenService.createToken(id, role, JwtTokenType.ACCESS);
        String newHash = jwtTokenService.hashRefreshToken(newRefreshToken);

        Instant now = Instant.now(clock);
        if (!expiresAt.isAfter(now) || refreshTokenRepository.replaceRefreshTokenHash(oldHash, newHash, now) != 1) {
            throw new JwtException("Refresh token is invalid or has expired");
        }

        return new RefreshTokenPairResponse(accessToken, newRefreshToken);
    }
}
