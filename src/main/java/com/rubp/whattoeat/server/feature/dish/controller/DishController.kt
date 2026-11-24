package com.rubp.whattoeat.server.feature.dish.controller

import com.rubp.whattoeat.server.feature.dish.model.DishResponse
import com.rubp.whattoeat.server.feature.dish.service.DishService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stalls/{stallId}/dishes")
class DishController(
    private val dishService: DishService
) {
    @GetMapping
    fun listDishes(@PathVariable("stallId") stallId: Long): List<DishResponse> {
        return dishService.listDishes(stallId)
    }
}
