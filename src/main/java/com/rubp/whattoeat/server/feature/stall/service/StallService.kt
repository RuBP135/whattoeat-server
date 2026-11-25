package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.data.StallEntry
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.model.StallResponse
import org.springframework.stereotype.Service

@Service
class StallService(
    private val stallRepository: StallRepository,
    private val stallCategoryRepository: StallCategoryRepository
) {
    fun listStalls(categoryId: Long?, locationId: Long?): List<StallResponse> {
        return stallRepository.findAll(locationId = locationId, categoryId = categoryId, active = true).map { stall ->
            StallResponse(stall.id, stall.slot, stall.name, stall.location.id)
        }
    }

    fun getStall(stallId: Long): StallResponse {
        val stall = requireActiveStall(stallId)
        return StallResponse(stall.id, stall.slot, stall.name, stall.location.id)
    }

    fun listCategories(): List<StallCategoryResponse> {
        return stallCategoryRepository.findAllByOrderByIdAsc().map { category ->
            StallCategoryResponse(category.id, category.name)
        }
    }

    fun listStallCategories(stallId: Long): List<StallCategoryResponse> {
        requireActiveStall(stallId)

        return stallCategoryRepository.findByStallId(stallId).map { category ->
            StallCategoryResponse(category.id, category.name)
        }
    }

    fun requireActiveStall(stallId: Long): StallEntry {
        val stall = stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
        if (!stall.active) {
            throw StallNotFoundException()
        }
        return stall
    }
}
