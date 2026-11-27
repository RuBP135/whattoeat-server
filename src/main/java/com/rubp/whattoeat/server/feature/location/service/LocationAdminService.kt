package com.rubp.whattoeat.server.feature.location.service

import com.rubp.whattoeat.server.core.data.isForeignKeyViolation
import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import com.rubp.whattoeat.server.feature.location.data.BuildingRepository
import com.rubp.whattoeat.server.feature.location.data.LocationEntry
import com.rubp.whattoeat.server.feature.location.data.LocationRepository
import com.rubp.whattoeat.server.feature.location.exception.BuildingNotFoundException
import com.rubp.whattoeat.server.feature.location.exception.InvalidLocationOrderException
import com.rubp.whattoeat.server.feature.location.exception.LocationAlreadyExistsException
import com.rubp.whattoeat.server.feature.location.exception.LocationInUseException
import com.rubp.whattoeat.server.feature.location.exception.LocationNotFoundException
import com.rubp.whattoeat.server.feature.location.model.LocationRequest
import com.rubp.whattoeat.server.feature.location.model.LocationResponse
import com.rubp.whattoeat.server.feature.location.model.ReorderLocationsRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LocationAdminService(
    private val locationRepository: LocationRepository,
    private val buildingRepository: BuildingRepository,
    private val adminAuditService: AdminAuditService
) {
    @Transactional
    fun createLocation(buildingId: Long, request: LocationRequest): LocationResponse {
        val buildingName = buildingRepository.findNameByIdForUpdate(buildingId)
            ?: throw BuildingNotFoundException()
        val ordinal = (locationRepository.findMaxOrdinalByBuildingId(buildingId) ?: -1) + 1
        val location = try {
            locationRepository.save(
                LocationEntry(
                    building = buildingRepository.getReferenceById(buildingId),
                    name = request.name,
                    ordinal = ordinal
                )
            )
        } catch (exception: DataIntegrityViolationException) {
            throw locationWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.CREATE,
            AuditResourceType.LOCATION,
            location.id,
            "audit.location.create",
            location.name,
            buildingName
        )
        return LocationResponse(location.id, location.name)
    }

    @Transactional
    fun renameLocation(locationId: Long, request: LocationRequest) {
        val oldName = locationRepository.findNameByIdForUpdate(locationId)
            ?: throw LocationNotFoundException()
        if (oldName == request.name) {
            return
        }
        try {
            locationRepository.updateNameById(locationId, request.name)
        } catch (exception: DataIntegrityViolationException) {
            throw locationWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.LOCATION,
            locationId,
            "audit.location.rename",
            oldName,
            request.name
        )
    }

    @Transactional
    fun deleteLocation(locationId: Long) {
        val locationName = locationRepository.findNameByIdForUpdate(locationId)
            ?: throw LocationNotFoundException()
        try {
            locationRepository.deleteById(locationId)
            locationRepository.flush()
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isForeignKeyViolation()) {
                throw LocationInUseException()
            }
            throw exception
        }
        adminAuditService.record(
            AuditOperation.DELETE,
            AuditResourceType.LOCATION,
            locationId,
            "audit.location.delete",
            locationName
        )
    }

    @Transactional
    fun reorderLocations(buildingId: Long, request: ReorderLocationsRequest) {
        val buildingName = buildingRepository.findNameByIdForUpdate(buildingId)
            ?: throw BuildingNotFoundException()
        val currentLocationIds = locationRepository.findIdsByBuildingIdForUpdate(buildingId)
        val locationIds = request.locationIds
        if (locationIds.size != currentLocationIds.size || locationIds.toSet() != currentLocationIds.toSet()) {
            throw InvalidLocationOrderException()
        }
        locationIds.forEachIndexed { ordinal, locationId ->
            locationRepository.updateOrdinalById(locationId, ordinal)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.BUILDING,
            buildingId,
            "audit.building.locations_reordered",
            buildingName,
            locationIds
        )
    }

    private fun locationWriteFailure(exception: DataIntegrityViolationException): RuntimeException {
        return if (exception.isUniqueViolation()) {
            LocationAlreadyExistsException()
        } else {
            exception
        }
    }
}
