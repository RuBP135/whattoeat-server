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
        name = "email_registration_session",
        indexes = {
                @Index(columnList = "registration_expires_at"),
                @Index(columnList = "invitation_id")
        }
)
public class EmailRegistrationSessionEntry {

    @Id
    @Column(name = "session_code", length = 16, nullable = false, updatable = false)
    private byte[] sessionCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private InvitationEntry invitation;

    // 282 = 12 字节 IV + 最多 254 字节邮箱明文 + 16 字节 GCM 认证标签
    @Column(name = "email_ciphertext", nullable = false, updatable = false, length = 282)
    private byte[] emailCiphertext;

    @Column(name = "email_hmac", nullable = false, updatable = false, length = 32)
    private byte[] emailHmac;

    @Column(name = "verification_code_hmac", nullable = false, length = 32)
    private byte[] verificationCodeHmac;

    @Column(name = "registration_expires_at", nullable = false, updatable = false)
    private Instant registrationExpiresAt;

    @Column(nullable = false)
    private boolean verified;


    protected EmailRegistrationSessionEntry() { }

    public EmailRegistrationSessionEntry(
            byte[] sessionCode,
            InvitationEntry invitation,
            byte[] emailCiphertext,
            byte[] emailHmac,
            byte[] verificationCodeHmac,
            Instant registrationExpiresAt
    ) {
        this.sessionCode = sessionCode.clone();
        this.invitation = invitation;
        this.emailCiphertext = emailCiphertext;
        this.emailHmac = emailHmac;
        this.verificationCodeHmac = verificationCodeHmac;
        this.registrationExpiresAt = registrationExpiresAt;
    }


    public byte[] getSessionCode() {
        return sessionCode.clone();
    }

    public InvitationEntry getInvitation() {
        return invitation;
    }

    public byte[] getEmailCiphertext() {
        return emailCiphertext;
    }

    public byte[] getEmailHmac() {
        return emailHmac;
    }

    public byte[] getVerificationCodeHmac() {
        return verificationCodeHmac;
    }

    public Instant getRegistrationExpiresAt() {
        return registrationExpiresAt;
    }

    public void setVerificationCodeHmac(byte[] verificationCodeHmac) {
        this.verificationCodeHmac = verificationCodeHmac;
    }

    public boolean isVerified() {
        return verified;
    }

    public void markVerified() {
        this.verified = true;
    }
}
