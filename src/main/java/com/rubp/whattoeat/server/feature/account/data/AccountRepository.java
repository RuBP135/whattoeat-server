package com.rubp.whattoeat.server.feature.account.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntry, Long> {
}
