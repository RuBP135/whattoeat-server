package com.rubp.whattoeat.server.feature.audit.service

import com.rubp.whattoeat.server.feature.account.data.AccountRepository
import com.rubp.whattoeat.server.feature.audit.data.AdminAuditLogEntry
import com.rubp.whattoeat.server.feature.audit.data.AdminAuditLogRepository
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import org.jetbrains.annotations.PropertyKey
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.Locale

@Service
class AdminAuditService(
    private val adminAuditLogRepository: AdminAuditLogRepository,
    private val accountRepository: AccountRepository,
    private val clock: Clock,
    private val messageSource: MessageSource
) {
    private val log = LoggerFactory.getLogger(AdminAuditService::class.java)

    @Transactional(propagation = Propagation.MANDATORY)
    fun record(
        operation: AuditOperation,
        resourceType: AuditResourceType,
        resourceId: Long?,
        @PropertyKey(resourceBundle = "i18n.messages") summaryKey: String,
        vararg summaryArgs: Any
    ) {
        val adminId = getCurrentAdminId()
        val summary = messageSource.getMessage(
            summaryKey,
            Array(summaryArgs.size) { summaryArgs[it].toString() },
            Locale.ENGLISH,
        )

        val savedEntry = adminAuditLogRepository.save(
            AdminAuditLogEntry(
                admin = accountRepository.getReferenceById(adminId),
                resourceType = resourceType,
                resourceId = resourceId,
                operation = operation,
                summaryKey = summaryKey,
                summaryArgs = summaryArgs.map { it.toString() },
                createdAt = Instant.now(clock)
            )
        )

        log.info(
            "Admin audit recorded: id={}, adminId={}, operation={}, resourceType={}, resourceId={}, summary={}",
            savedEntry.id,
            adminId,
            operation,
            resourceType,
            resourceId,
            summary
        )
    }

    private fun getCurrentAdminId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw IllegalStateException("Missing authenticated admin in SecurityContext")
        }

        val subject = when (val principal = authentication.principal) {
            is Jwt -> principal.subject
            else -> authentication.name
        }

        if (subject.isNullOrBlank()) {
            throw IllegalStateException("Missing admin subject in authentication")
        }

        return subject.toLongOrNull()
            ?: throw IllegalStateException("Invalid admin id in authentication subject: $subject")
    }
}
