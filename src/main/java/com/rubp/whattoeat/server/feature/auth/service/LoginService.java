package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.model.LoginRequest;
import com.rubp.whattoeat.server.feature.auth.model.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LoginService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "用户名或密码错误";

    private final JwtTokenService jwtTokenService;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(
            JwtTokenService jwtTokenService,
            AccountRepository accountRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtTokenService = jwtTokenService;
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AccountEntry account = accountRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountUnavailableException();
        }

        long id = account.getId();
        String accessToken = jwtTokenService.createToken(id, JwtTokenType.ACCESS);
        Jwt refreshJwt = jwtTokenService.createJwt(id, JwtTokenType.REFRESH);
        String refreshToken = refreshJwt.getTokenValue();

        String refreshTokenHash = jwtTokenService.hashRefreshToken(refreshToken);
        Instant expiresAt = refreshJwt.getExpiresAt();
        refreshTokenRepository.save(new RefreshTokenEntry(account, refreshTokenHash, expiresAt));

        return new LoginResponse(accessToken, refreshToken);
    }
}
