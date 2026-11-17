package com.rubp.whattoeat.server.feature.auth.data;

import com.rubp.whattoeat.server.feature.account.data.AccountEntry;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private AccountEntry account;

    @Column(name = "refresh_token_hash", nullable = false, unique = true)
    private String refreshTokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;


    protected RefreshTokenEntry() { }

    public RefreshTokenEntry(AccountEntry account, String refreshTokenHash, Instant expiresAt) {
        this.account = account;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
    }


    public Long getId() {
        return id;
    }

    public AccountEntry getAccount() {
        return account;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
