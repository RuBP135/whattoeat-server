package com.rubp.whattoeat.server.feature.audit.model

import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import java.time.Instant

data class AdminAuditLogResponse(
    val id: Long,
    val adminId: Long,
    val adminName: String?,
    val operation: AuditOperation,
    val resourceType: AuditResourceType,
    val resourceId: Long?,
    val summary: String,
    val createdAt: Instant
)
