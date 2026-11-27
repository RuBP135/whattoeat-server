package com.rubp.whattoeat.server.feature.location.controller

import com.rubp.whattoeat.server.feature.location.model.CampusRequest
import com.rubp.whattoeat.server.feature.location.model.CampusResponse
import com.rubp.whattoeat.server.feature.location.service.CampusAdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/campuses")
class CampusAdminController(
    private val campusAdminService: CampusAdminService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCampus(@Valid @RequestBody request: CampusRequest): CampusResponse {
        return campusAdminService.createCampus(request)
    }

    @PatchMapping("/{campusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun renameCampus(
        @PathVariable("campusId") campusId: Long,
        @Valid @RequestBody request: CampusRequest
    ) {
        campusAdminService.renameCampus(campusId, request)
    }

    @DeleteMapping("/{campusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCampus(@PathVariable("campusId") campusId: Long) {
        campusAdminService.deleteCampus(campusId)
    }
}
