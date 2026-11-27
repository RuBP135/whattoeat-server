package com.rubp.whattoeat.server.feature.audit.controller

import com.rubp.whattoeat.server.core.model.PageResponse
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.model.AdminAuditLogResponse
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/audits")
class AdminAuditController(
    private val adminAuditService: AdminAuditService
) {
    @GetMapping
    fun listAuditLogs(
        @RequestParam("operation", required = false) operation: AuditOperation?,
        @RequestParam("resourceType", required = false) resourceType: AuditResourceType?,
        @RequestParam("page", defaultValue = "0") @Min(0) page: Int,
        @RequestParam("size", defaultValue = "20") @Min(1) @Max(100) size: Int
    ): PageResponse<AdminAuditLogResponse> {
        return adminAuditService.listAuditLogs(operation, resourceType, page, size)
    }
}
