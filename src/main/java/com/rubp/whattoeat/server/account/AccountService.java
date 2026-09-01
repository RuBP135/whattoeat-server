package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.exception.UniqueUidGenerateException;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class AccountService {

    private static final long MIN_UID = 1_000_000_000L;
    private static final long UID_BOUND = 10_000_000_000L;
    private static final int ATTEMPT_LIMIT = 5;

    private static final String UID_UNIQUE_CONSTRAINT = "unique_account_uid";

    private final SecureRandom secureRandom = new SecureRandom();

    private final AccountRepository accountRepository;


    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public AccountEntry createAnonymousAccount() {
        for(int attemp = 0; attemp < ATTEMPT_LIMIT; ++attemp){
            String uid = generateUid();

            if(accountRepository.existsByUid(uid)) continue;

            try {
                return accountRepository.saveAndFlush(new AccountEntry(
                        uid,
                        Role.USER,
                        AccountStatus.ACTIVE,
                        Instant.now()
                ));
            } catch (DataIntegrityViolationException exception){
                if(!isUidUniqueViolation(exception)){
                    throw exception;
                }
            }
        }

        throw new UniqueUidGenerateException("无法创建唯一的uid");
    }


    private String generateUid() {
        long value = secureRandom.nextLong(MIN_UID, UID_BOUND);
        return Long.toString(value);
    }

    private boolean isUidUniqueViolation(DataIntegrityViolationException exception){

        Throwable cause = exception;

        while(cause != null){
            if(cause instanceof ConstraintViolationException violationException
                    && UID_UNIQUE_CONSTRAINT.equals(violationException.getConstraintName())){
                return true;
            }
            cause = cause.getCause();
        }
        return false;

    }
}
