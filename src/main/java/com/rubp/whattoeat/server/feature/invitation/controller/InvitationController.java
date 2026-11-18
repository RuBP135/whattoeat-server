package com.rubp.whattoeat.server.feature.invitation.controller;

import com.rubp.whattoeat.server.feature.invitation.model.CreateInvitationResponse;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public CreateInvitationResponse createInvitation() {
        return new CreateInvitationResponse(invitationService.createInvitation());
    }
}
