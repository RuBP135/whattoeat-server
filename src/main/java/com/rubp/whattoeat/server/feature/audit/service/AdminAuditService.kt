package com.rubp.whattoeat.server.feature.audit.service

import com.rubp.whattoeat.server.core.model.PageResponse
import com.rubp.whattoeat.server.feature.account.data.AccountRepository
import com.rubp.whattoeat.server.feature.audit.data.AdminAuditLogEntry
import com.rubp.whattoeat.server.feature.audit.data.AdminAuditLogRepository
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.model.AdminAuditLogResponse
import org.jetbrains.annotations.PropertyKey
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
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

    /**
     * 记录管理员操作审计日志。
     *
     * 必须在已有事务和已认证管理员上下文中调用。
     *
     * @param operation 操作类型
     * @param resourceType 被操作资源的类型
     * @param resourceId 被操作资源的 ID；无具体目标资源时可以传 `null`
     * @param summaryKey 审计摘要的国际化消息键（定义于 `i18n/messages*.properties`，编写新的记录时，你应当先去这里创建模板）
     * @param summaryArgs 按顺序填充消息模板占位符（`{0}`、`{1}`…）的参数，具体数量与含义应该看你定义的模板
     */
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

    @Transactional(readOnly = true)
    fun listAuditLogs(
        operation: AuditOperation?,
        resourceType: AuditResourceType?,
        page: Int,
        size: Int
    ): PageResponse<AdminAuditLogResponse> {
        val total = adminAuditLogRepository.countAll(operation, resourceType)
        val offset = page.toLong() * size
        if (total == 0L || offset >= total || offset > Int.MAX_VALUE) {
            return PageResponse(emptyList(), page, size, total)
        }

        val locale = LocaleContextHolder.getLocale()
        val items = adminAuditLogRepository.findPage(
            operation,
            resourceType,
            size,
            offset.toInt()
        ).map { entry ->
            AdminAuditLogResponse(
                id = entry.id,
                adminId = entry.admin.id,
                adminName = entry.admin.username,
                operation = entry.operation,
                resourceType = entry.resourceType,
                resourceId = entry.resourceId,
                summary = resolveSummary(entry.summaryKey, entry.summaryArgs, locale),
                createdAt = entry.createdAt
            )
        }
        return PageResponse(items, page, size, total)
    }

    private fun resolveSummary(
        summaryKey: String,
        summaryArgs: List<String>,
        locale: Locale
    ): String {
        val resolvedArgs = summaryArgs.toTypedArray()
        return messageSource.getMessage(summaryKey, resolvedArgs, summaryKey, locale) ?: summaryKey
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
