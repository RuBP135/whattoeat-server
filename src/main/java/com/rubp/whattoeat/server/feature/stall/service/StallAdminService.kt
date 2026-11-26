package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import com.rubp.whattoeat.server.feature.location.data.LocationRepository
import com.rubp.whattoeat.server.feature.stall.data.StallCategoryRepository
import com.rubp.whattoeat.server.feature.stall.data.StallEntry
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.LocationUnavailableException
import com.rubp.whattoeat.server.feature.stall.exception.StallCategoryUnavailableException
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import com.rubp.whattoeat.server.feature.stall.exception.StallSlotOccupiedException
import com.rubp.whattoeat.server.feature.stall.model.AdminStallResponse
import com.rubp.whattoeat.server.feature.stall.model.ChangeStallActiveRequest
import com.rubp.whattoeat.server.feature.stall.model.CreateStallRequest
import com.rubp.whattoeat.server.feature.stall.model.ReplaceStallCategoriesRequest
import com.rubp.whattoeat.server.feature.stall.model.StallCategoryResponse
import com.rubp.whattoeat.server.feature.stall.model.UpdateStallRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StallAdminService(
    private val stallRepository: StallRepository,
    private val stallCategoryRepository: StallCategoryRepository,
    private val locationRepository: LocationRepository,
    private val adminAuditService: AdminAuditService
) {
    fun listStalls(locationId: Long?, categoryId: Long?, active: Boolean?): List<AdminStallResponse> {
        return stallRepository.findAll(locationId, categoryId, active).map { stall ->
            AdminStallResponse(stall.id, stall.slot, stall.name, stall.location.id, stall.active)
        }
    }

    fun getStall(stallId: Long): AdminStallResponse {
        val stall = stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
        return AdminStallResponse(stall.id, stall.slot, stall.name, stall.location.id, stall.active)
    }

    @Transactional(readOnly = true)
    fun listCategoriesByStallId(stallId: Long): List<StallCategoryResponse> {
        requireStall(stallId)

        return stallCategoryRepository.findByStallId(stallId).map { category ->
            StallCategoryResponse(category.id, category.name)
        }
    }

    @Transactional
    fun createStall(request: CreateStallRequest): AdminStallResponse {
        val location = locationRepository.findById(request.locationId)
            .orElseThrow { LocationUnavailableException() }
        val stall = stallRepository.save(
            StallEntry(slot = request.slot, name = request.name, location = location, active = false)
        )
        adminAuditService.record(
            AuditOperation.CREATE,
            AuditResourceType.STALL,
            stall.id,
            "audit.stall.create",
            stall.name,
            location.name,
            stall.slot
        )

        return AdminStallResponse(stall.id, stall.slot, stall.name, location.id, stall.active)
    }

    @Transactional
    fun updateStall(stallId: Long, request: UpdateStallRequest) {
        val locationId = request.locationId
        val location = locationId?.let {
            locationRepository.findById(it).orElseThrow {
                requireStall(stallId)
                LocationUnavailableException()
            }
        }

        val updated = try {
            stallRepository.updateById(stallId, request.slot, request.name, locationId)
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isUniqueViolation("uk_stall_location_active_slot")) {
                throw StallSlotOccupiedException()
            }
            throw exception
        }
        if (updated == 0) {
            requireStall(stallId)
            return
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.STALL,
            stallId,
            "audit.stall.update",
            request.name ?: "-",
            request.slot ?: "-",
            location?.name ?: "-"
        )
    }

    @Transactional
    fun changeStallActive(stallId: Long, request: ChangeStallActiveRequest) {
        val updated = try {
            stallRepository.updateActiveById(stallId, request.active)
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isUniqueViolation("uk_stall_location_active_slot")) {
                throw StallSlotOccupiedException()
            }
            throw exception
        }
        if (updated == 0) {
            throw StallNotFoundException()
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.STALL,
            stallId,
            if (request.active) "audit.stall.activate" else "audit.stall.deactivate"
        )
    }

    @Transactional
    fun replaceCategories(stallId: Long, request: ReplaceStallCategoriesRequest) {
        if (stallRepository.findIdForUpdate(stallId) == null) {
            throw StallNotFoundException()
        }

        stallCategoryRepository.deleteLinksByStallId(stallId)
        val categoryIds = request.categoryIds
        if (categoryIds.isNotEmpty()) {
            val inserted = stallCategoryRepository.insertLinks(stallId, categoryIds)
            if (inserted != categoryIds.size) {
                throw StallCategoryUnavailableException()
            }
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.STALL,
            stallId,
            "audit.stall.categories_updated",
            categoryIds.sorted()
        )
    }

    @Transactional
    fun deleteStall(stallId: Long) {
        stallRepository.findIdForUpdate(stallId) ?: throw StallNotFoundException()
        stallCategoryRepository.deleteLinksByStallId(stallId)
        stallRepository.deleteById(stallId)
        adminAuditService.record(
            AuditOperation.DELETE,
            AuditResourceType.STALL,
            stallId,
            "audit.stall.delete",
            stallId
        )
    }

    private fun requireStall(stallId: Long) {
        stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
    }
}
