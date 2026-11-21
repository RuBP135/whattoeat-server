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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TokenRefreshServiceTest {

    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String REFRESH_TOKEN_HASH = "refresh-token-hash";
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = NOW.plusSeconds(300);

    private final JwtDecoder jwtRefreshDecoder = mock(JwtDecoder.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final TokenRefreshService tokenRefreshService = new TokenRefreshService(
            jwtRefreshDecoder,
            jwtTokenService,
            refreshTokenRepository,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );


    @ParameterizedTest
    @CsvSource({"123, USER", Long.MAX_VALUE + ", ADMIN"})
    public void refreshTokenPair_shouldRotateTokensForActiveAccount(long id, Role role) {
        String accessToken = "access-token";
        String newRefreshToken = "new-refresh-token";
        String newHash = "new-refresh-token-hash";

        mockDecodedRefreshToken(id);
        mockStoredRefreshToken(id, role, AccountStatus.ACTIVE);
        when(jwtTokenService.createRefreshToken(id, role, EXPIRES_AT)).thenReturn(newRefreshToken);
        when(jwtTokenService.createToken(id, role, JwtTokenType.ACCESS)).thenReturn(accessToken);
        when(jwtTokenService.hashRefreshToken(newRefreshToken)).thenReturn(newHash);
        when(refreshTokenRepository.replaceRefreshTokenHash(REFRESH_TOKEN_HASH, newHash, NOW)).thenReturn(1);

        RefreshTokenPairResponse response =
                tokenRefreshService.refreshTokenPair(new RefreshTokenPairRequest(REFRESH_TOKEN));

        assertEquals(accessToken, response.accessToken());
        assertEquals(newRefreshToken, response.refreshToken());

        verify(refreshTokenRepository).replaceRefreshTokenHash(REFRESH_TOKEN_HASH, newHash, NOW);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"124", "str", "9223372036854775808", "0", "-1"})
    public void refreshTokenPair_shouldRejectSubjectNotMatchingStoredAccount(String subject) {
        Jwt jwt = mock(Jwt.class);
        when(jwtRefreshDecoder.decode(REFRESH_TOKEN)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(subject);
        when(jwt.getExpiresAt()).thenReturn(EXPIRES_AT);
        mockStoredRefreshToken(123L, Role.USER, AccountStatus.ACTIVE);

        assertThrows(
                JwtException.class,
                () -> tokenRefreshService.refreshTokenPair(new RefreshTokenPairRequest(REFRESH_TOKEN))
        );

        verify(jwtTokenService, never()).createRefreshToken(anyLong(), any(), any());
        verify(jwtTokenService, never()).createToken(anyLong(), any(), any());
        verify(refreshTokenRepository, never()).replaceRefreshTokenHash(any(), any(), any());
    }

    @Test
    public void refreshTokenPair_shouldThrowWhenRefreshTokenInvalid(){
        String refreshToken = "invalid-refresh-token";
        JwtException jwtException = new JwtException("refresh token 不合法");

        when(jwtRefreshDecoder.decode(refreshToken)).thenThrow(jwtException);

        assertThrows(
                JwtException.class,
                () -> tokenRefreshService.refreshTokenPair(
                        new RefreshTokenPairRequest(refreshToken)
                )
        );
    }

    @Test
    public void refreshTokenPair_shouldThrowWhenTokenIsNotRegistered() {
        mockDecodedRefreshToken(123L);
        when(jwtTokenService.hashRefreshToken(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(refreshTokenRepository.findByRefreshTokenHash(REFRESH_TOKEN_HASH)).thenReturn(Optional.empty());

        assertThrows(
                JwtException.class,
                () -> tokenRefreshService.refreshTokenPair(new RefreshTokenPairRequest(REFRESH_TOKEN))
        );
    }

    @Test
    public void refreshTokenPair_shouldThrowWhenAccountDisabled() {
        long id = 123L;
        mockDecodedRefreshToken(id);
        mockStoredRefreshToken(id, Role.USER, AccountStatus.DISABLED);

        assertThrows(
                AccountUnavailableException.class,
                () -> tokenRefreshService.refreshTokenPair(new RefreshTokenPairRequest(REFRESH_TOKEN))
        );
    }

    private void mockDecodedRefreshToken(long id) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(Long.toString(id));
        when(jwt.getExpiresAt()).thenReturn(EXPIRES_AT);
        when(jwtRefreshDecoder.decode(REFRESH_TOKEN)).thenReturn(jwt);
    }

    private void mockStoredRefreshToken(long id, Role role, AccountStatus status) {
        AccountEntry account = mock(AccountEntry.class);
        when(account.getId()).thenReturn(id);
        when(account.getRole()).thenReturn(role);
        when(account.getStatus()).thenReturn(status);
        when(jwtTokenService.hashRefreshToken(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(refreshTokenRepository.findByRefreshTokenHash(REFRESH_TOKEN_HASH))
                .thenReturn(Optional.of(new RefreshTokenEntry(account, REFRESH_TOKEN_HASH, EXPIRES_AT)));
    }

}