package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryEntry
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryAlreadyExistsException
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryNotFoundException
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StallCategoryAdminService(
    private val stallCategoryRepository: StallCategoryRepository
) {
    fun createCategory(request: StallCategoryRequest) {
        try {
            stallCategoryRepository.save(StallCategoryEntry(name = request.name))
        } catch (exception: DataIntegrityViolationException) {
            throw categoryWriteFailure(exception)
        }
    }

    fun renameCategory(categoryId: Long, request: StallCategoryRequest) {
        val matchedRows = try {
            stallCategoryRepository.updateName(categoryId, request.name)
        } catch (exception: DataIntegrityViolationException) {
            throw categoryWriteFailure(exception)
        }
        if (matchedRows == 0) {
            throw StallCategoryNotFoundException()
        }
    }

    @Transactional
    fun deleteCategory(categoryId: Long) {
        stallCategoryRepository.deleteStallLinksByCategoryId(categoryId)
        if (stallCategoryRepository.deleteCategoryById(categoryId) == 0) {
            throw StallCategoryNotFoundException()
        }
    }

    private fun categoryWriteFailure(exception: DataIntegrityViolationException): RuntimeException {
        return if (exception.isUniqueViolation()) {
            StallCategoryAlreadyExistsException()
        } else {
            exception
        }
    }
}
