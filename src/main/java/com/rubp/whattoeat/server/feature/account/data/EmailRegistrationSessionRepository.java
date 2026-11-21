package com.rubp.whattoeat.server.feature.account.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRegistrationSessionRepository extends JpaRepository<EmailRegistrationSessionEntry, byte[]> {
}
