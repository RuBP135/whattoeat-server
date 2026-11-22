package com.rubp.whattoeat.server.feature.stall.controller

import com.rubp.whattoeat.server.feature.stall.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.model.StallResponse
import com.rubp.whattoeat.server.feature.stall.service.StallService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stalls")
class StallController(
    private val stallService: StallService
) {
    @GetMapping
    fun listStalls(@RequestParam("categoryId", required = false) categoryId: Long?): List<StallResponse> {
        return stallService.listStalls(categoryId)
    }

    @GetMapping("/{stallId}/dishes")
    fun listDishes(@PathVariable("stallId") stallId: Long): List<DishResponse> {
        return stallService.listDishes(stallId)
    }

    @GetMapping("/categories")
    fun listCategories(): List<StallCategoryResponse> {
        return stallService.listCategories()
    }

    @GetMapping("/{stallId}/categories")
    fun listStallCategories(@PathVariable("stallId") stallId: Long): List<StallCategoryResponse> {
        return stallService.listStallCategories(stallId)
    }
}
