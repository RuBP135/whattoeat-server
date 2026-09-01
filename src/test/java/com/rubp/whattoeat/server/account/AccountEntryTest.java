package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccountEntryTest {

    @Test
    public void activateShouldSetStatusToActive() {
        AccountEntry account = new AccountEntry(
                "1234567899",
                Role.USER,
                AccountStatus.DISABLED,
                Instant.now()
        );

        account.activate();

        assertNotNull(account);
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        account.activate();

        assertNotNull(account);
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    public void disableShouldSetStatusToDisable() {
        AccountEntry account = new AccountEntry(
                "1234567899",
                Role.USER,
                AccountStatus.ACTIVE,
                Instant.now()
        );

        account.disable();

        assertNotNull(account);
        assertEquals(AccountStatus.DISABLED, account.getStatus());

        account.disable();

        assertNotNull(account);
        assertEquals(AccountStatus.DISABLED, account.getStatus());
    }
}
