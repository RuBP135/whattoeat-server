package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.exception.InvalidInvitationCodeException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountRequest;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenEntry;
import com.rubp.whattoeat.server.feature.auth.data.RefreshTokenRepository;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final InvitationService invitationService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;

    public AccountService(
            AccountRepository accountRepository,
            InvitationService invitationService,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.invitationService = invitationService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.clock = clock;
    }

    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        if (!invitationService.consumeInvitation(request.invitationCode())) {
            throw new InvalidInvitationCodeException();
        }

        String passwordHash = passwordEncoder.encode(request.password());

        Role role = Role.USER;

        AccountEntry account = accountRepository.save(new AccountEntry(
                request.username(),
                passwordHash,
                role,
                AccountStatus.ACTIVE,
                Instant.now(clock)
        ));

        long id = account.getId();
        String accessToken = jwtTokenService.createToken(id, role, JwtTokenType.ACCESS);
        Jwt refreshJwt = jwtTokenService.createJwt(id, role, JwtTokenType.REFRESH);
        String refreshToken = refreshJwt.getTokenValue();

        String refreshTokenHash = jwtTokenService.hashRefreshToken(refreshToken);
        Instant expiresAt = refreshJwt.getExpiresAt();

        refreshTokenRepository.save(new RefreshTokenEntry(account, refreshTokenHash, expiresAt));

        return new CreateAccountResponse(accessToken, refreshToken);
    }
}
