package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.exception.UniqueUidGenerateException;
import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;


import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    private static final int ATTEMPT_LIMIT = 5;

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountService accountService = new AccountService(accountRepository);

    @Test
    public void createAnonymousAccount_shouldCreateActiveUserAccount(){

        when(accountRepository.existsByUid(anyString())).thenReturn(false);
        when(accountRepository.saveAndFlush(any(AccountEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountEntry account = accountService.createAnonymousAccountEntry();

        assertNotNull(account);
        assertTrue(account.getUid().matches("\\d{10}"));
        assertEquals(Role.USER, account.getRole());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    public void createAnonymousAccount_shouldRetryWhenUidAlreadyExists(){

        when(accountRepository.existsByUid(anyString()))
                .thenReturn(true)
                .thenReturn(false);
        when(accountRepository.saveAndFlush(any(AccountEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountEntry account = accountService.createAnonymousAccountEntry();

        verify(accountRepository, times(2)).existsByUid(anyString());
        verify(accountRepository, times(1)).saveAndFlush(any(AccountEntry.class));

        assertNotNull(account);
        assertTrue(account.getUid().matches("\\d{10}"));
    }

    @Test
    public void createAnonymousAccount_shouldThrowAfterAttemptLimit(){

        when(accountRepository.existsByUid(anyString())).thenReturn(true);

        assertThrows(
                UniqueUidGenerateException.class,
                accountService::createAnonymousAccountEntry
        );

        verify(accountRepository, times(ATTEMPT_LIMIT)).existsByUid(anyString());
        verify(accountRepository, never()).saveAndFlush(any(AccountEntry.class));
    }

    @Test
    public void createAnonymousAccount_shouldThrowOtherException(){

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("其他数据库异常");

        when(accountRepository.saveAndFlush(any(AccountEntry.class)))
                .thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        DataIntegrityViolationException.class,
                        accountService::createAnonymousAccountEntry
                )
        );

    }

    @Test
    public void createAnonymousAccount_shouldRetryAndSuccess() {

        SQLException sqlException =
                new SQLIntegrityConstraintViolationException("uid 已存在");

        ConstraintViolationException violationException = new ConstraintViolationException(
                "uid 唯一约束冲突",
                sqlException,
                ConstraintViolationException.ConstraintKind.UNIQUE,
                "unique_account_uid"
        );

        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "uid已经存在异常",
                violationException
        );

        when(accountRepository.saveAndFlush(any(AccountEntry.class)))
                .thenThrow(exception)
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountEntry account = accountService.createAnonymousAccountEntry();

        verify(accountRepository, times(2)).existsByUid(anyString());
        verify(accountRepository, times(2)).saveAndFlush(any(AccountEntry.class));

        assertNotNull(account);
        assertTrue(account.getUid().matches("\\d{10}"));
    }
}
