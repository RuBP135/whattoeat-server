package com.rubp.whattoeat.server.feature.account.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "email_limit")
public class EmailLimitEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_ciphertext", nullable = false, updatable = false, length = 282)
    private byte[] emailCiphertext;

    @Column(name = "email_hmac", nullable = false, unique = true, updatable = false, length = 32)
    private byte[] emailHmac;

    @Column(name = "next_send_allowed_at", nullable = false)
    private Instant nextSendAllowedAt;


    protected EmailLimitEntry() { }

    public EmailLimitEntry(byte[] emailCiphertext, byte[] emailHmac, Instant nextSendAllowedAt) {
        this.emailCiphertext = emailCiphertext;
        this.emailHmac = emailHmac;
        this.nextSendAllowedAt = nextSendAllowedAt;
    }


    public Long getId() {
        return id;
    }

    public byte[] getEmailCiphertext() {
        return emailCiphertext;
    }

    public byte[] getEmailHmac() {
        return emailHmac;
    }

    public Instant getNextSendAllowedAt() {
        return nextSendAllowedAt;
    }
}
