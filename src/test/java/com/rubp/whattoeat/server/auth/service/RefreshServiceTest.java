package com.rubp.whattoeat.server.auth.service;

import com.rubp.whattoeat.server.account.AccountEntry;
import com.rubp.whattoeat.server.account.AccountRepository;
import com.rubp.whattoeat.server.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;
import com.rubp.whattoeat.server.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.auth.model.RefreshAccessTokenRequest;
import com.rubp.whattoeat.server.auth.model.RefreshAccessTokenResponse;
import org.junit.jupiter.api.Test;
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


    @Test
    public void refreshAccessToken_shouldCreateAccessTokenForActiveAccount() {
        String refreshToken = "refresh-token";
        String uid = "1234567890";
        String accessToken = "access-token";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(uid);
        when(jwtRefreshDecoder.decode(anyString())).thenReturn(jwt);

        AccountEntry account = new AccountEntry(
                uid,
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now()
        );

        when(accountRepository.findByUid(uid)).thenReturn(Optional.of(account));
        when(jwtTokenService.createToken(uid, JwtTokenType.ACCESS)).thenReturn(accessToken);

        RefreshAccessTokenResponse response =
                refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken));

        assertEquals(accessToken, response.accessToken());

        verify(jwtRefreshDecoder).decode(refreshToken);
        verify(accountRepository).findByUid(uid);
        verify(jwtTokenService).createToken(uid, JwtTokenType.ACCESS);
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
    public void refreshAccessToken_shouldThrowWhenUidNotExist() {
        String refreshToken = "refresh-token";
        String uid = "1234567890";

        Jwt jwt = mock(Jwt.class);
        when(jwtRefreshDecoder.decode(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(uid);

        AccountUnavailableException accountUnavailableException = new AccountUnavailableException();
        when(accountRepository.existsByUid(uid)).thenThrow(accountUnavailableException);

        assertThrows(
                AccountUnavailableException.class,
                () -> refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken))
        );
    }

    @Test
    public void refreshAccessToken_shouldThrowWhenAccountDisabled() {
        String refreshToken = "refresh-token";
        String uid = "1234567890";

        Jwt jwt = mock(Jwt.class);
        when(jwtRefreshDecoder.decode(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(uid);

        AccountEntry account = new AccountEntry(
                uid,
                Role.USER,
                AccountStatus.DISABLED,
                Instant.now()
        );

        when(accountRepository.findByUid(uid)).thenReturn(Optional.of(account));

        assertThrows(
                AccountUnavailableException.class,
                () -> refreshService.refreshAccessToken(new RefreshAccessTokenRequest(refreshToken))
        );
    }

}