package com.rubp.whattoeat.server.feature.stall.controller

import com.rubp.whattoeat.server.feature.stall.model.ReplaceStallCategoriesRequest
import com.rubp.whattoeat.server.feature.stall.service.StallAdminService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/stalls")
class StallAdminController(
    private val stallAdminService: StallAdminService
) {
    @PutMapping("/{stallId}/categories")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun replaceCategories(
        @PathVariable("stallId") stallId: Long,
        @RequestBody request: ReplaceStallCategoriesRequest
    ) {
        stallAdminService.replaceCategories(stallId, request)
    }
}
