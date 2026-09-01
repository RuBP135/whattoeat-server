package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountServiceTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountService accountService = new AccountService(accountRepository);

    @Test
    public void createAnonymousAccount_shouldCreateActiveUserAccount(){
        when(accountRepository.existsByUid(anyString())).thenReturn(false);
        when(accountRepository.saveAndFlush(any(AccountEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountEntry account = accountService.createAnonymousAccount();

        assertNotNull(account);
        assertTrue(account.getUid().matches("\\d{10}"));
        assertEquals(Role.USER, account.getRole());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNotNull(account.getCreatedAt());
    }
}
