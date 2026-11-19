package com.rubp.whattoeat.server.feature.account.data;

import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
        name = "email_verification_session",
        indexes = {
                @Index(columnList = "expires_at"),
                @Index(columnList = "invitation_id"),
                @Index(columnList = "email_id")
        }
)
public class EmailVerificationSessionEntry {

    @Id
    @Column(length = 16, nullable = false, updatable = false)
    private byte[] id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private InvitationEntry invitation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_id", nullable = false, updatable = false)
    private EmailLimitEntry emailLimit;

    @Column(name = "code_hmac", nullable = false, length = 32)
    private byte[] codeHmac;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;


    protected EmailVerificationSessionEntry() { }

    public EmailVerificationSessionEntry(
            byte[] id,
            InvitationEntry invitation,
            EmailLimitEntry emailLimit,
            byte[] codeHmac,
            Instant expiresAt
    ) {
        this.id = id.clone();
        this.invitation = invitation;
        this.emailLimit = emailLimit;
        this.codeHmac = codeHmac;
        this.expiresAt = expiresAt;
    }


    public byte[] getId() {
        return id.clone();
    }

    public InvitationEntry getInvitation() {
        return invitation;
    }

    public EmailLimitEntry getEmailLimit() {
        return emailLimit;
    }

    public byte[] getCodeHmac() {
        return codeHmac;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
