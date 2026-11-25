package com.rubp.whattoeat.server.feature.stall.controller

import com.rubp.whattoeat.server.feature.stall.model.AdminStallResponse
import com.rubp.whattoeat.server.feature.stall.model.ReplaceStallCategoriesRequest
import com.rubp.whattoeat.server.feature.stall.service.StallAdminService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @PutMapping("/{stallId}/categories")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun replaceCategories(
        @PathVariable("stallId") stallId: Long,
        @RequestBody request: ReplaceStallCategoriesRequest
    ) {
        stallAdminService.replaceCategories(stallId, request)
    }
}
