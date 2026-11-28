package com.rubp.whattoeat.server.feature.audit.data

import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AdminAuditLogRepository : JpaRepository<AdminAuditLogEntry, Long> {
    @Query(
        """
        select log
        from AdminAuditLogEntry log
        join fetch log.admin
        where (:resourceType is null or log.resourceType = :resourceType)
          and (:operation is null or log.operation = :operation)
        order by log.createdAt desc, log.id desc
        limit :size offset :offset
        """
    )
    fun findPage(
        @Param("resourceType") resourceType: AuditResourceType?,
        @Param("operation") operation: AuditOperation?,
        @Param("size") size: Int,
        @Param("offset") offset: Int
    ): List<AdminAuditLogEntry>

    @Query(
        """
        select count(log)
        from AdminAuditLogEntry log
        where (:operation is null or log.operation = :operation)
          and (:resourceType is null or log.resourceType = :resourceType)
        """
    )
    fun countAll(
        @Param("operation") operation: AuditOperation?,
        @Param("resourceType") resourceType: AuditResourceType?
    ): Long
}
