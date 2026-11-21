package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.feature.stall.data.DishRepository
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import com.rubp.whattoeat.server.feature.stall.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.model.StallResponse
import org.springframework.stereotype.Service

@Service
class StallService(
    private val stallRepository: StallRepository,
    private val dishRepository: DishRepository
) {
    fun listStalls(): List<StallResponse> {
        return stallRepository.findActiveStalls().map { stall ->
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
}
