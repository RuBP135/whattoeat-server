package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryUnavailableException
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import com.rubp.whattoeat.server.feature.stall.model.ReplaceStallCategoriesRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StallAdminService(
    private val stallRepository: StallRepository,
    private val stallCategoryRepository: StallCategoryRepository
) {
    @Transactional
    fun replaceCategories(stallId: Long, request: ReplaceStallCategoriesRequest) {
        if (stallRepository.findIdForUpdate(stallId) == null) {
            throw StallNotFoundException()
        }

        stallCategoryRepository.deleteLinksByStallId(stallId)
        val categoryIds = request.categoryIds
        if (categoryIds.isNotEmpty()) {
            val inserted = stallCategoryRepository.insertLinks(stallId, categoryIds)
            if (inserted != categoryIds.size) {
                throw StallCategoryUnavailableException()
            }
        }
    }
}
