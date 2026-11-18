package com.rubp.whattoeat.server.feature.invitation.service;

import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import com.rubp.whattoeat.server.feature.invitation.domain.InvitationCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class InvitationService {

    private final InvitationCodeGenerator invitationCodeGenerator = new InvitationCodeGenerator();

    private final InvitationRepository invitationRepository;
    private final Clock clock;
    private final Duration ttl;

    public InvitationService(
            InvitationRepository invitationRepository,
            Clock clock,
            @Value("${invitation.ttl}") Duration ttl
    ) {
        if(ttl == null || !ttl.isPositive()){
            throw new IllegalArgumentException("邀请码有效期必须大于零");
        }

        this.invitationRepository = invitationRepository;
        this.clock = clock;
        this.ttl = ttl;
    }

    public String createInvitation() {
        Instant expiresAt = Instant.now(clock).plus(ttl);
        String code = invitationCodeGenerator.generate();

        invitationRepository.save(new InvitationEntry(
                Base64.getUrlDecoder().decode(code),
                expiresAt
        ));

        return code;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean consumeInvitation(String invitationCode) {
        byte[] code;
        try {
            code = Base64.getUrlDecoder().decode(invitationCode);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        if (!Base64.getUrlEncoder().withoutPadding().encodeToString(code).equals(invitationCode)) {
            return false;
        }

        return invitationRepository.deleteValidInvitation(code, Instant.now(clock)) == 1;
    }
}
