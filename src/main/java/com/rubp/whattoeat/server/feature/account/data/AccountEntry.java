package com.rubp.whattoeat.server.feature.account.data;

import com.rubp.whattoeat.server.feature.account.model.AccountStatus;
import com.rubp.whattoeat.server.feature.account.model.Role;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "account")
public class AccountEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    protected AccountEntry() { }

    public AccountEntry(String username, String passwordHash, Role role, AccountStatus status, Instant createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }


    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public void disable() {
        this.status = AccountStatus.DISABLED;
    }


    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
