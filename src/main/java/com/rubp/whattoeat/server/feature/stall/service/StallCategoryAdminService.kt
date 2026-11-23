package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryEntry
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryAlreadyExistsException
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class StallCategoryAdminService(
    private val stallCategoryRepository: StallCategoryRepository
) {
    fun createCategory(request: StallCategoryRequest) {
        try {
            stallCategoryRepository.save(StallCategoryEntry(name = request.name))
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isUniqueViolation()) {
                throw StallCategoryAlreadyExistsException()
            }
            throw exception
        }
    }
}
