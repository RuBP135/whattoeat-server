package com.rubp.whattoeat.server.feature.location.controller

import com.rubp.whattoeat.server.feature.location.model.CampusTreeResponse
import com.rubp.whattoeat.server.feature.location.service.LocationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/locations")
class LocationController(
    private val locationService: LocationService
) {
    @GetMapping
    fun listLocations(): List<CampusTreeResponse> {
        return locationService.listLocations()
    }
}
