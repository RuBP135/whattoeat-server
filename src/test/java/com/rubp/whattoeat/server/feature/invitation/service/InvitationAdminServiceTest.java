package com.rubp.whattoeat.server.feature.invitation.service;

import com.rubp.whattoeat.server.feature.invitation.data.InvitationEntry;
import com.rubp.whattoeat.server.feature.invitation.data.InvitationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvitationAdminServiceTest {

    private final InvitationRepository invitationRepository = mock(InvitationRepository.class);
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final Duration ttl = Duration.ofHours(24);
    private final InvitationAdminService invitationAdminService = new InvitationAdminService(invitationRepository, clock, ttl);

    @Test
    public void createInvitation_shouldSaveRawCodeAndExpirationThenReturnEncodedCode() {
        String code = invitationAdminService.createInvitation();

        ArgumentCaptor<InvitationEntry> invitationCaptor = ArgumentCaptor.forClass(InvitationEntry.class);
        verify(invitationRepository).save(invitationCaptor.capture());
        InvitationEntry invitation = invitationCaptor.getValue();

        assertArrayEquals(Base64.getUrlDecoder().decode(code), invitation.getCode());
        assertEquals(Instant.now(clock).plus(ttl), invitation.getExpiresAt());
    }
}
