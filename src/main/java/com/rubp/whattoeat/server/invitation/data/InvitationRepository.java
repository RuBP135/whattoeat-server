package com.rubp.whattoeat.server.invitation.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<InvitationEntry, Long> {
}
