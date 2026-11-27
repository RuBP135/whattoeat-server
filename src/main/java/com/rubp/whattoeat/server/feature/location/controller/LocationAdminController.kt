package com.rubp.whattoeat.server.feature.location.controller

import com.rubp.whattoeat.server.feature.location.model.LocationRequest
import com.rubp.whattoeat.server.feature.location.model.LocationResponse
import com.rubp.whattoeat.server.feature.location.model.ReorderLocationsRequest
import com.rubp.whattoeat.server.feature.location.service.LocationAdminService
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
class LocationAdminController(
    private val locationAdminService: LocationAdminService
) {
    @PostMapping("/buildings/{buildingId}/locations")
    @ResponseStatus(HttpStatus.CREATED)
    fun createLocation(
        @PathVariable("buildingId") buildingId: Long,
        @Valid @RequestBody request: LocationRequest
    ): LocationResponse {
        return locationAdminService.createLocation(buildingId, request)
    }

    @PatchMapping("/locations/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun renameLocation(
        @PathVariable("locationId") locationId: Long,
        @Valid @RequestBody request: LocationRequest
    ) {
        locationAdminService.renameLocation(locationId, request)
    }

    @DeleteMapping("/locations/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLocation(@PathVariable("locationId") locationId: Long) {
        locationAdminService.deleteLocation(locationId)
    }

    @PutMapping("/buildings/{buildingId}/locations/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reorderLocations(
        @PathVariable("buildingId") buildingId: Long,
        @RequestBody request: ReorderLocationsRequest
    ) {
        locationAdminService.reorderLocations(buildingId, request)
    }
}
