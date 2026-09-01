package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.exception.UniqueUidGenerateException;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final SecureRandom secureRandom = new SecureRandom();

    private final AccountRepository accountRepository;


    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public AccountEntry createAnonymousAccount() {

        log.info("正在创建匿名账户");

        for(int attempt = 0; attempt < ATTEMPT_LIMIT; ++attempt){
            String uid = generateUid();

            if(accountRepository.existsByUid(uid)) continue;

            try {
                AccountEntry account = accountRepository.saveAndFlush(new AccountEntry(
                        uid,
                        Role.USER,
                        AccountStatus.ACTIVE,
                        Instant.now()
                ));

                log.info("创建了一个匿名账户");

                return account;

            } catch (DataIntegrityViolationException exception){
                if(!isUidUniqueViolation(exception)){
                    throw exception;
                }

                log.warn("生成了重复的uid，尝试次数{}", attempt + 1);
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
