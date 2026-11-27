package com.rubp.whattoeat.server.feature.invitation.controller;

import com.rubp.whattoeat.server.feature.invitation.model.CreateInvitationResponse;
import com.rubp.whattoeat.server.feature.invitation.service.InvitationAdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/invitations")
public class InvitationAdminController {

    private final InvitationAdminService invitationAdminService;

    public InvitationAdminController(InvitationAdminService invitationAdminService) {
        this.invitationAdminService = invitationAdminService;
    }

    @PostMapping
    public CreateInvitationResponse createInvitation() {
        return new CreateInvitationResponse(invitationAdminService.createInvitation());
    }
}
