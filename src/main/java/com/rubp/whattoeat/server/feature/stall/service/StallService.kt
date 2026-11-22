package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.feature.stall.data.DishRepository
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import com.rubp.whattoeat.server.feature.stall.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.model.StallResponse
import org.springframework.stereotype.Service

@Service
class StallService(
    private val stallRepository: StallRepository,
    private val dishRepository: DishRepository,
    private val stallCategoryRepository: StallCategoryRepository
) {
    fun listStalls(categoryId: Long?, locationId: Long?): List<StallResponse> {
        val stalls = if (categoryId != null && locationId != null) {
            stallRepository.findActiveStallsByLocationIdAndCategoryId(locationId, categoryId)
        } else if (locationId != null) {
            stallRepository.findActiveStallsByLocationId(locationId)
        } else if (categoryId != null) {
            stallRepository.findActiveStallsByCategoryId(categoryId)
        } else {
            stallRepository.findActiveStalls()
        }

        return stalls.map { stall ->
            StallResponse(stall.id, stall.name, stall.location.id)
        }
    }

    fun listDishes(stallId: Long): List<DishResponse> {
        requireActiveStall(stallId)

        return dishRepository.findByStallId(stallId).map { dish ->
            DishResponse(dish.id, dish.name)
        }
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

    private fun requireActiveStall(stallId: Long) {
        val stall = stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
        if (!stall.active) {
            throw StallNotFoundException()
        }
    }
}
