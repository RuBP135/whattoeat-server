package com.rubp.whattoeat.server.feature.location.service

import com.rubp.whattoeat.server.feature.location.data.BuildingRepository
import com.rubp.whattoeat.server.feature.location.data.CampusRepository
import com.rubp.whattoeat.server.feature.location.data.LocationRepository
import com.rubp.whattoeat.server.feature.location.model.BuildingTreeResponse
import com.rubp.whattoeat.server.feature.location.model.CampusTreeResponse
import com.rubp.whattoeat.server.feature.location.model.LocationResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LocationService(
    private val campusRepository: CampusRepository,
    private val buildingRepository: BuildingRepository,
    private val locationRepository: LocationRepository
) {
    @Transactional(readOnly = true)
    fun listLocations(): List<CampusTreeResponse> {
        val campuses = campusRepository.findAll()
        val buildings = buildingRepository.findAll()
        val locations = locationRepository.findAll()

        val locationsByBuildingId = locations.groupBy { it.building.id }
        val buildingsByCampusId = buildings.groupBy { it.campus.id }

        return campuses.map { campus ->
            val campusBuildings = buildingsByCampusId[campus.id].orEmpty().map { building ->
                val buildingLocations = locationsByBuildingId[building.id].orEmpty().map { location ->
                    LocationResponse(location.id, location.name)
                }
                BuildingTreeResponse(building.id, building.name, building.kind, buildingLocations)
            }
            CampusTreeResponse(campus.id, campus.name, campusBuildings)
        }
    }
}
