package com.rubp.whattoeat.server.feature.account.service;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import com.rubp.whattoeat.server.feature.account.data.AccountRepository;
import com.rubp.whattoeat.server.feature.account.exception.InvalidInvitationCodeException;
import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountRequest;
import com.rubp.whattoeat.server.feature.account.model.CreateAccountResponse;
import com.rubp.whattoeat.server.feature.account.model.Role;
import com.rubp.whattoeat.server.feature.auth.service.SessionTokenService;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final InvitationService invitationService;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;
    private final Clock clock;

    public AccountService(
            AccountRepository accountRepository,
            InvitationService invitationService,
            PasswordEncoder passwordEncoder,
            SessionTokenService sessionTokenService,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.invitationService = invitationService;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
        this.clock = clock;
    }

    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        if (!invitationService.consumeInvitation(request.invitationCode())) {
            throw new InvalidInvitationCodeException();
        }

        String passwordHash = passwordEncoder.encode(request.password());

        AccountEntry account = accountRepository.save(new AccountEntry(
                request.username(),
                passwordHash,
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now(clock)
        ));

        var tokens = sessionTokenService.issueTokenPair(account);
        return new CreateAccountResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
