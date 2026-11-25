package com.rubp.whattoeat.server.feature.stall.controller

import com.rubp.whattoeat.server.feature.stall.model.AdminStallResponse
import com.rubp.whattoeat.server.feature.stall.model.CreateStallRequest
import com.rubp.whattoeat.server.feature.stall.model.ReplaceStallCategoriesRequest
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.model.UpdateStallRequest
import com.rubp.whattoeat.server.feature.stall.service.StallAdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/stalls")
class StallAdminController(
    private val stallAdminService: StallAdminService
) {
    @GetMapping
    fun listStalls(
        @RequestParam("locationId", required = false) locationId: Long?,
        @RequestParam("categoryId", required = false) categoryId: Long?,
        @RequestParam("active", required = false) active: Boolean?
    ): List<AdminStallResponse> {
        return stallAdminService.listStalls(locationId, categoryId, active)
    }

    @GetMapping("/{stallId}")
    fun getStall(@PathVariable("stallId") stallId: Long): AdminStallResponse {
        return stallAdminService.getStall(stallId)
    }

    @GetMapping("/{stallId}/categories")
    fun listStallCategories(@PathVariable("stallId") stallId: Long): List<StallCategoryResponse> {
        return stallAdminService.listStallCategories(stallId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createStall(@Valid @RequestBody request: CreateStallRequest): AdminStallResponse {
        return stallAdminService.createStall(request)
    }

    @PatchMapping("/{stallId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateStall(
        @PathVariable("stallId") stallId: Long,
        @Valid @RequestBody request: UpdateStallRequest
    ) {
        stallAdminService.updateStall(stallId, request)
    }

    @PutMapping("/{stallId}/categories")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun replaceCategories(
        @PathVariable("stallId") stallId: Long,
        @RequestBody request: ReplaceStallCategoriesRequest
    ) {
        stallAdminService.replaceCategories(stallId, request)
    }
}
