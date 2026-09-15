package com.rubp.whattoeat.server.auth.service;

import com.rubp.whattoeat.server.account.AccountEntry;
import com.rubp.whattoeat.server.account.AccountService;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;
import com.rubp.whattoeat.server.auth.model.CreateAnonymousAccountResponse;
import com.rubp.whattoeat.server.auth.model.JwtTokenType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnonymousAuthServiceTest {

    private final AccountService accountService = mock(AccountService.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final AnonymousAuthService anonymousAuthService = new AnonymousAuthService(
            accountService,
            jwtTokenService
    );

    @Test
    public void createAnonymousAccount_shouldReturnActiveResponse() {
        String uid = "1234567890";

        AccountEntry account = new AccountEntry(
                uid,
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now()
        );

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(accountService.createAnonymousAccountEntry()).thenReturn(account);
        when(jwtTokenService.createToken(uid,JwtTokenType.ACCESS)).thenReturn(accessToken);
        when(jwtTokenService.createToken(uid, JwtTokenType.REFRESH)).thenReturn(refreshToken);

        CreateAnonymousAccountResponse response = anonymousAuthService.createAnonymousAccount();

        assertNotNull(response);
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
        assertEquals(uid, response.uid());

        verify(accountService).createAnonymousAccountEntry();
        verify(jwtTokenService).createToken(uid, JwtTokenType.ACCESS);
        verify(jwtTokenService).createToken(uid, JwtTokenType.REFRESH);
    }
}