package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountRequest;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.auth.model.JwtTokenType;
import com.rubp.whattoeat.server.feature.auth.service.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.clock = clock;
    }

    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());

        AccountEntry account = accountRepository.save(new AccountEntry(
                request.username(),
                passwordHash,
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now(clock)
        ));

        long id = account.getId();
        String accessToken = jwtTokenService.createToken(id, JwtTokenType.ACCESS);
        String refreshToken = jwtTokenService.createToken(id, JwtTokenType.REFRESH);

        return new CreateAccountResponse(accessToken, refreshToken);
    }
}
