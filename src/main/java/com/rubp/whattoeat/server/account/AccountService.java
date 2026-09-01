package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class AccountService {

    private static final long MIN_UID = 1_000_000_000L;
    private static final long UID_BOUND = 10_000_000_000L;
    private static final int ATTEMPT_LIMIT = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    private final AccountRepository accountRepository;


    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public AccountEntry createAnonymousAccount() {
        String uid;

        do {
            uid = generateUid();
        } while(accountRepository.existsByUid(uid));

        AccountEntry account = new AccountEntry(
                uid,
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now()
        );

        return accountRepository.save(account);
    }


    private String generateUid() {
        long value = secureRandom.nextLong(MIN_UID, UID_BOUND);
        return Long.toString(value);
    }
}
