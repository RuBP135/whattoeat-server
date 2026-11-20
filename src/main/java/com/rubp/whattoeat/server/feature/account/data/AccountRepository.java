package com.rubp.whattoeat.server.feature.account.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntry, Long> {
    Optional<AccountEntry> findByUsername(String username);

    boolean existsByEmailHmac(byte[] emailHmac);
}
