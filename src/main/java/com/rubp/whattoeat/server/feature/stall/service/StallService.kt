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
    fun listStalls(categoryId: Long?): List<StallResponse> {
        val stalls = if (categoryId == null) {
            stallRepository.findActiveStalls()
        } else {
            stallRepository.findActiveStallsByCategoryId(categoryId)
        }

        return stalls.map { stall ->
            StallResponse(stall.id, stall.name, stall.location.id)
        }
    }

    fun listDishes(stallId: Long): List<DishResponse> {
        val stall = stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
        if (!stall.active) {
            throw StallNotFoundException()
        }

        return dishRepository.findByStallId(stallId).map { dish ->
            DishResponse(dish.id, dish.name)
        }
    }

    fun listCategories(): List<StallCategoryResponse> {
        return stallCategoryRepository.findAllByOrderByIdAsc().map { category ->
            StallCategoryResponse(category.id, category.name)
        }
    }
}
