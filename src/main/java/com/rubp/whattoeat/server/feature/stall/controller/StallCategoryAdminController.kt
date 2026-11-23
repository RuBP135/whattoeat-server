package com.rubp.whattoeat.server.feature.stall.controller

import com.rubp.whattoeat.server.feature.stall.model.StallCategoryRequest
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.service.StallCategoryAdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/stalls/categories")
class StallCategoryAdminController(
    private val stallCategoryAdminService: StallCategoryAdminService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createCategory(@Valid @RequestBody request: StallCategoryRequest) {
        stallCategoryAdminService.createCategory(request)
    }

    @PatchMapping("/{categoryId}")
    fun renameCategory(
        @PathVariable("categoryId") categoryId: Long,
        @Valid @RequestBody request: StallCategoryRequest
    ): StallCategoryResponse {
        return stallCategoryAdminService.renameCategory(categoryId, request)
    }
}
