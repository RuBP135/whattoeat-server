package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryEntry
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryAlreadyExistsException
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryNotFoundException
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryRequest
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StallCategoryAdminService(
    private val stallCategoryRepository: StallCategoryRepository,
    private val adminAuditService: AdminAuditService
) {
    @Transactional
    fun createCategory(request: StallCategoryRequest): StallCategoryResponse {
        val category = try {
            stallCategoryRepository.save(StallCategoryEntry(name = request.name))
        } catch (exception: DataIntegrityViolationException) {
            throw categoryWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.CREATE,
            AuditResourceType.STALL_CATEGORY,
            category.id,
            "audit.stall.category.create",
            category.name
        )
        return StallCategoryResponse(category.id, category.name)
    }

    @Transactional
    fun renameCategory(categoryId: Long, request: StallCategoryRequest) {
        val oldName = stallCategoryRepository.findNameByIdForUpdate(categoryId)
            ?: throw StallCategoryNotFoundException()
        if (oldName == request.name) {
            return
        }
        try {
            stallCategoryRepository.updateNameById(categoryId, request.name)
        } catch (exception: DataIntegrityViolationException) {
            throw categoryWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.STALL_CATEGORY,
            categoryId,
            "audit.stall.category.rename",
            oldName,
            request.name
        )
    }

    @Transactional
    fun deleteCategory(categoryId: Long) {
        val categoryName = stallCategoryRepository.findNameByIdForUpdate(categoryId)
            ?: throw StallCategoryNotFoundException()
        stallCategoryRepository.deleteLinksByCategoryId(categoryId)
        stallCategoryRepository.deleteById(categoryId)
        adminAuditService.record(
            AuditOperation.DELETE,
            AuditResourceType.STALL_CATEGORY,
            categoryId,
            "audit.stall.category.delete",
            categoryName
        )
    }

    private fun categoryWriteFailure(exception: DataIntegrityViolationException): RuntimeException {
        return if (exception.isUniqueViolation()) {
            StallCategoryAlreadyExistsException()
        } else {
            exception
        }
    }
}
