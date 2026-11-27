package com.rubp.whattoeat.server.feature.location.service

import com.rubp.whattoeat.server.core.data.isForeignKeyViolation
import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import com.rubp.whattoeat.server.feature.location.data.BuildingEntry
import com.rubp.whattoeat.server.feature.location.data.BuildingRepository
import com.rubp.whattoeat.server.feature.location.data.CampusRepository
import com.rubp.whattoeat.server.feature.location.exception.BuildingAlreadyExistsException
import com.rubp.whattoeat.server.feature.location.exception.BuildingInUseException
import com.rubp.whattoeat.server.feature.location.exception.BuildingNotFoundException
import com.rubp.whattoeat.server.feature.location.exception.CampusNotFoundException
import com.rubp.whattoeat.server.feature.location.exception.InvalidBuildingOrderException
import com.rubp.whattoeat.server.feature.location.model.BuildingResponse
import com.rubp.whattoeat.server.feature.location.model.CreateBuildingRequest
import com.rubp.whattoeat.server.feature.location.model.ReorderBuildingsRequest
import com.rubp.whattoeat.server.feature.location.model.UpdateBuildingRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BuildingAdminService(
    private val buildingRepository: BuildingRepository,
    private val campusRepository: CampusRepository,
    private val adminAuditService: AdminAuditService
) {
    @Transactional
    fun createBuilding(campusId: Long, request: CreateBuildingRequest): BuildingResponse {
        val campusName = campusRepository.findNameByIdForUpdate(campusId)
            ?: throw CampusNotFoundException()
        val ordinal = (buildingRepository.findMaxOrdinalByCampusId(campusId) ?: -1) + 1
        val building = try {
            buildingRepository.save(
                BuildingEntry(
                    campus = campusRepository.getReferenceById(campusId),
                    name = request.name,
                    kind = request.kind,
                    ordinal = ordinal
                )
            )
        } catch (exception: DataIntegrityViolationException) {
            throw buildingWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.CREATE,
            AuditResourceType.BUILDING,
            building.id,
            "audit.building.create",
            building.name,
            campusName
        )
        return BuildingResponse(building.id, building.name, building.kind)
    }

    @Transactional
    fun updateBuilding(buildingId: Long, request: UpdateBuildingRequest) {
        val oldName = buildingRepository.findNameByIdForUpdate(buildingId)
            ?: throw BuildingNotFoundException()
        try {
            buildingRepository.updateById(buildingId, request.name, request.kind?.name)
        } catch (exception: DataIntegrityViolationException) {
            throw buildingWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.BUILDING,
            buildingId,
            "audit.building.update",
            oldName,
            request.name ?: "-",
            request.kind?.name ?: "-"
        )
    }

    @Transactional
    fun deleteBuilding(buildingId: Long) {
        val buildingName = buildingRepository.findNameByIdForUpdate(buildingId)
            ?: throw BuildingNotFoundException()
        try {
            buildingRepository.deleteById(buildingId)
            buildingRepository.flush()
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isForeignKeyViolation()) {
                throw BuildingInUseException()
            }
            throw exception
        }
        adminAuditService.record(
            AuditOperation.DELETE,
            AuditResourceType.BUILDING,
            buildingId,
            "audit.building.delete",
            buildingName
        )
    }

    @Transactional
    fun reorderBuildings(campusId: Long, request: ReorderBuildingsRequest) {
        val campusName = campusRepository.findNameByIdForUpdate(campusId)
            ?: throw CampusNotFoundException()
        val currentBuildingIds = buildingRepository.findIdsByCampusIdForUpdate(campusId)
        val buildingIds = request.buildingIds
        if (buildingIds.size != currentBuildingIds.size || buildingIds.toSet() != currentBuildingIds.toSet()) {
            throw InvalidBuildingOrderException()
        }
        buildingIds.forEachIndexed { ordinal, buildingId ->
            buildingRepository.updateOrdinalById(buildingId, ordinal)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.CAMPUS,
            campusId,
            "audit.campus.buildings_reordered",
            campusName,
            buildingIds
        )
    }

    private fun buildingWriteFailure(exception: DataIntegrityViolationException): RuntimeException {
        return if (exception.isUniqueViolation()) {
            BuildingAlreadyExistsException()
        } else {
            exception
        }
    }
}
