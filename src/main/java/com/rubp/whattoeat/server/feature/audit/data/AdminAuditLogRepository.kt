package com.rubp.whattoeat.server.feature.audit.data

import org.springframework.data.jpa.repository.JpaRepository

interface AdminAuditLogRepository : JpaRepository<AdminAuditLogEntry, Long>
