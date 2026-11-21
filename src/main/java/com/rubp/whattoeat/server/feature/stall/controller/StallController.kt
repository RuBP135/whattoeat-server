package com.rubp.whattoeat.server.feature.stall.controller

import com.rubp.whattoeat.server.feature.stall.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.model.StallResponse
import com.rubp.whattoeat.server.feature.stall.service.StallService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stalls")
class StallController(
    private val stallService: StallService
) {
    @GetMapping
    fun listStalls(): List<StallResponse> {
        return stallService.listStalls()
    }

    @GetMapping("/{stallId}/dishes")
    fun listDishes(@PathVariable("stallId") stallId: Long): List<DishResponse> {
        return stallService.listDishes(stallId)
    }

    @GetMapping("/categories")
    fun listCategories(): List<StallCategoryResponse> {
        return stallService.listCategories()
    }
}
