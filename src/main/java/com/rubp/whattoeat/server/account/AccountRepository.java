package com.rubp.whattoeat.server.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntry, Long> {

    boolean existsByUid(String uid);

    Optional<AccountEntry> findByUid(String uid);
}
