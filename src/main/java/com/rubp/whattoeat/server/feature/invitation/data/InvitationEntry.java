package com.rubp.whattoeat.server.feature.invitation.data;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "invitation")
public class InvitationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 16)
    private byte[] code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;


    protected InvitationEntry() { }

    public InvitationEntry(byte[] code, Instant expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
    }


    public Long getId() {
        return id;
    }

    public byte[] getCode() {
        return code;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
