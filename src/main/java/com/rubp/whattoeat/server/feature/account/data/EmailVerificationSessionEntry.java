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
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Entity
@Table(
        name = "email_verification_session",
        indexes = {
                @Index(columnList = "registration_expires_at"),
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

    /**
     * 新建会话时为空，邮箱验证码校验通过后记录允许注册的截止时间
     */
    @Column(name = "registration_expires_at")
    private @Nullable Instant registrationExpiresAt;


    protected EmailVerificationSessionEntry() { }

    public EmailVerificationSessionEntry(
            byte[] id,
            InvitationEntry invitation,
            EmailLimitEntry emailLimit,
            byte[] codeHmac
    ) {
        this.id = id.clone();
        this.invitation = invitation;
        this.emailLimit = emailLimit;
        this.codeHmac = codeHmac;
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

    public @Nullable Instant getRegistrationExpiresAt() {
        return registrationExpiresAt;
    }

    public void setRegistrationExpiresAt(@Nullable Instant registrationExpiresAt) {
        this.registrationExpiresAt = registrationExpiresAt;
    }
}
