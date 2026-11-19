package com.rubp.whattoeat.server.feature.account.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailLimitRepository extends JpaRepository<EmailLimitEntry, Long> {
    Optional<EmailLimitEntry> findByEmailHmac(byte[] emailHmac);
}
