package com.rubp.whattoeat.server.account;

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
    @Column(nullable = false)
    private Role status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    protected AccountEntry() { }

    public AccountEntry(String uid, Role status, Instant createdAt) {
        this.uid = uid;
        this.status = status;
        this.createdAt = createdAt;
    }


    public void active() {
        this.status = Role.ACTIVE;
    }

    public void disable() {
        this.status = Role.DISABLED;
    }


    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public Role getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
