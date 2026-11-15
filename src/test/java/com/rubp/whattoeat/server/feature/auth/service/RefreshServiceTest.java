package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.model.RefreshAccessTokenRequest;
import com.rubp.whattoeat.server.feature.auth.model.RefreshAccessTokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RefreshServiceTest {

    private final JwtDecoder jwtRefreshDecoder = mock(JwtDecoder.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final RefreshService refreshService = new RefreshService(
            jwtRefreshDecoder,
            jwtTokenService,
            accountRepository
    );


    @ParameterizedTest
    @ValueSource(longs = {123L, Long.MAX_VALUE})
    public void refreshAccessToken_shouldCreateAccessTokenForActiveAccount(long id) {
        String refreshToken = "refresh-token";
        String accessToken = "access-token";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(Long.toString(id));
        when(jwtRefreshDecoder.decode(anyString())).thenReturn(jwt);

        AccountEntry account = new AccountEntry(
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now()
        );

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(jwtTokenService.createToken(id, JwtTokenType.ACCESS)).thenReturn(accessToken);

        RefreshAccessTokenResponse response =
                refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken));

        assertEquals(accessToken, response.accessToken());

        verify(jwtRefreshDecoder).decode(refreshToken);
        verify(accountRepository).findById(id);
        verify(jwtTokenService).createToken(id, JwtTokenType.ACCESS);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "invalid-id", "9223372036854775808", "0", "-1"})
    public void refreshAccessToken_shouldRejectInvalidAccountId(String subject) {
        String refreshToken = "refresh-token";
        Jwt jwt = mock(Jwt.class);
        when(jwtRefreshDecoder.decode(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(subject);

        assertThrows(
                JwtException.class,
                () -> refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken))
        );

        verifyNoInteractions(accountRepository, jwtTokenService);
    }

    @Test
    public void refreshAccessToken_shouldThrowWhenRefreshTokenInvalid(){
        String refreshToken = "invalid-refresh-token";
        JwtException jwtException = new JwtException("refresh token 不合法");

        when(jwtRefreshDecoder.decode(refreshToken)).thenThrow(jwtException);

        assertThrows(
                JwtException.class,
                () -> refreshService.refreshAccessToken(
                        new RefreshAccessTokenRequest(refreshToken)
                )
        );
    }

    @Test
    public void refreshAccessToken_shouldThrowWhenIdNotExist() {
        String refreshToken = "refresh-token";
        long id = 123L;

        Jwt jwt = mock(Jwt.class);
        when(jwtRefreshDecoder.decode(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(Long.toString(id));

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                AccountUnavailableException.class,
                () -> refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken))
        );
    }

    @Test
    public void refreshAccessToken_shouldThrowWhenAccountDisabled() {
        String refreshToken = "refresh-token";
        long id = 123L;

        Jwt jwt = mock(Jwt.class);
        when(jwtRefreshDecoder.decode(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(Long.toString(id));

        AccountEntry account = new AccountEntry(
                Role.USER,
                AccountStatus.DISABLED,
                Instant.now()
        );

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        assertThrows(
                AccountUnavailableException.class,
                () -> refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken))
        );
    }

}