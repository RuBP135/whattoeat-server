package com.rubp.whattoeat.server.feature.dish.service

import com.rubp.whattoeat.server.feature.dish.data.DishRepository
import com.rubp.whattoeat.server.feature.dish.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DishAdminService(
    private val dishRepository: DishRepository,
    private val stallRepository: StallRepository
) {
    @Transactional(readOnly = true)
    fun listDishesByStallId(stallId: Long): List<DishResponse> {
        stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }

        return dishRepository.findByStallId(stallId).map { dish ->
            DishResponse(dish.id, dish.name)
        }
    }
}
