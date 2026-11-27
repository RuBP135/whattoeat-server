package com.rubp.whattoeat.server.feature.location.controller

import com.rubp.whattoeat.server.feature.location.model.BuildingResponse
import com.rubp.whattoeat.server.feature.location.model.CreateBuildingRequest
import com.rubp.whattoeat.server.feature.location.model.ReorderBuildingsRequest
import com.rubp.whattoeat.server.feature.location.model.UpdateBuildingRequest
import com.rubp.whattoeat.server.feature.location.service.BuildingAdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class BuildingAdminController(
    private val buildingAdminService: BuildingAdminService
) {
    @PostMapping("/campuses/{campusId}/buildings")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBuilding(
        @PathVariable("campusId") campusId: Long,
        @Valid @RequestBody request: CreateBuildingRequest
    ): BuildingResponse {
        return buildingAdminService.createBuilding(campusId, request)
    }

    @PatchMapping("/buildings/{buildingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateBuilding(
        @PathVariable("buildingId") buildingId: Long,
        @Valid @RequestBody request: UpdateBuildingRequest
    ) {
        buildingAdminService.updateBuilding(buildingId, request)
    }

    @DeleteMapping("/buildings/{buildingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBuilding(@PathVariable("buildingId") buildingId: Long) {
        buildingAdminService.deleteBuilding(buildingId)
    }

    @PutMapping("/campuses/{campusId}/buildings/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reorderBuildings(
        @PathVariable("campusId") campusId: Long,
        @RequestBody request: ReorderBuildingsRequest
    ) {
        buildingAdminService.reorderBuildings(campusId, request)
    }
}
