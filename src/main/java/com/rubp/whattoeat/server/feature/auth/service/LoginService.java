package com.rubp.whattoeat.server.feature.auth.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.exception.AccountUnavailableException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.auth.model.LoginRequest;
import com.rubp.whattoeat.server.feature.auth.model.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid username or password";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;

    public LoginService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            SessionTokenService sessionTokenService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
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

        var tokens = sessionTokenService.issueTokenPair(account);
        return new LoginResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
