package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryEntry
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryAlreadyExistsException
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryRequest
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class StallCategoryAdminService(
    private val stallCategoryRepository: StallCategoryRepository
) {
    fun createCategory(request: StallCategoryRequest): StallCategoryResponse {
        try {
            val category = stallCategoryRepository.save(StallCategoryEntry(name = request.name))
            return StallCategoryResponse(category.id, category.name)
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isUniqueViolation()) {
                throw StallCategoryAlreadyExistsException()
            }
            throw exception
        }
    }
}
