package com.rubp.whattoeat.server.feature.audit.data

import com.rubp.whattoeat.server.feature.account.data.AccountEntry
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(
    name = "admin_audit_log",
    indexes = [
        Index(columnList = "created_at, id"),
        Index(columnList = "resource_type, operation, created_at DESC, id DESC"),
        Index(columnList = "admin_id")
    ]
)
class AdminAuditLogEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false, updatable = false)
    var admin: AccountEntry,

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, updatable = false, length = 30)
    var resourceType: AuditResourceType,

    @Column(name = "resource_id", updatable = false)
    var resourceId: Long?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    var operation: AuditOperation,

    @Column(name = "summary_key", nullable = false, updatable = false, length = 100)
    var summaryKey: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_args", nullable = false, updatable = false)
    var summaryArgs: List<String>,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant
)
