package com.rubp.whattoeat.server.feature.location.service

import com.rubp.whattoeat.server.feature.location.data.LocationRepository
import com.rubp.whattoeat.server.feature.location.model.LocationResponse
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val locationRepository: LocationRepository
) {
    fun listLocations(): List<LocationResponse> {
        return locationRepository.findAll().map { location ->
            LocationResponse(location.id, location.name, location.kind)
        }
    }
}
