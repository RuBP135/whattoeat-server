package com.rubp.whattoeat.server.feature.dish.service

import com.rubp.whattoeat.server.feature.dish.data.DishRepository
import com.rubp.whattoeat.server.feature.dish.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.service.StallService
import org.springframework.stereotype.Service

@Service
class DishService(
    private val dishRepository: DishRepository,
    private val stallService: StallService
) {
    fun listDishes(stallId: Long): List<DishResponse> {
        stallService.requireActiveStall(stallId)

        return dishRepository.findByStallId(stallId).map { dish ->
            DishResponse(dish.id, dish.name)
        }
    }
}
