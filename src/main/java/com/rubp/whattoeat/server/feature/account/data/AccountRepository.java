package com.rubp.whattoeat.server.feature.account.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntry, Long> {
    Optional<AccountEntry> findByUsername(String username);

    Optional<AccountEntry> findByEmailHmac(byte[] emailHmac);

    boolean existsByEmailHmac(byte[] emailHmac);

    @Modifying
    @Query(value = """
        insert into account (
            username, password_hash, email_ciphertext,
            email_hmac, role, status, created_at
        )
        select :#{#account.username}, :#{#account.passwordHash},
               :#{#account.emailCiphertext}, :#{#account.emailHmac},
               :#{#account.role.name()}, :#{#account.status.name()},
               :#{#account.createdAt}
        where not exists (
            select 1
            from account
            where email_hmac = :#{#account.emailHmac}
        )
    """, nativeQuery = true)
    int insertIfEmailAbsent(AccountEntry account);

}
