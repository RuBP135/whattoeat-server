package com.rubp.whattoeat.server.feature.invitation.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface InvitationRepository extends JpaRepository<InvitationEntry, Long> {

    @Modifying
    @Query("""
            delete from InvitationEntry invitation
            where invitation.code = :code
              and invitation.expiresAt > :now
            """)
    int deleteValidInvitation(@Param("code") byte[] code, @Param("now") Instant now);

    @Modifying
    @Query("""
            delete from InvitationEntry invitation
            where invitation.expiresAt <= :now
            """)
    int deleteExpiredInvitations(@Param("now") Instant now);
}
