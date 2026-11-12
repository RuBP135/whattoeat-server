package com.rubp.whattoeat.server.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntry, Long> {
}
