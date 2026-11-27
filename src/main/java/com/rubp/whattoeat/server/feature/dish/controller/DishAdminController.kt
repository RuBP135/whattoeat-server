package com.rubp.whattoeat.server.feature.dish.controller

import com.rubp.whattoeat.server.feature.dish.model.DishResponse
import com.rubp.whattoeat.server.feature.dish.service.DishAdminService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class DishAdminController(
    private val dishAdminService: DishAdminService
) {
    @GetMapping("/stalls/{stallId}/dishes")
    fun listDishesByStallId(@PathVariable("stallId") stallId: Long): List<DishResponse> {
        return dishAdminService.listDishesByStallId(stallId)
    }
}
