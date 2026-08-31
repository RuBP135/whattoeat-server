package com.rubp.whattoeat.server.account;

import com.rubp.whattoeat.server.account.model.AccountStatus;
import com.rubp.whattoeat.server.account.model.Role;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "account")
public class AccountEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 10)
    private String uid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    protected AccountEntry() { }

    public AccountEntry(String uid, Role role, AccountStatus status, Instant createdAt) {
        this.uid = uid;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }


    public void active() {
        this.status = AccountStatus.ACTIVE;
    }

    public void disable() {
        this.status = AccountStatus.DISABLED;
    }


    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
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
