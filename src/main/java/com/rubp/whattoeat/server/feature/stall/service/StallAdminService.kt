package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
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
    private val locationRepository: LocationRepository
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
    fun listStallCategories(stallId: Long): List<StallCategoryResponse> {
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

        return AdminStallResponse(stall.id, stall.slot, stall.name, location.id, stall.active)
    }

    fun updateStall(stallId: Long, request: UpdateStallRequest) {
        val locationId = request.locationId
        if (locationId != null) {
            locationRepository.findById(locationId).orElseThrow {
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
        }
    }

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
    }

    @Transactional
    fun deleteStall(stallId: Long) {
        stallRepository.findIdForUpdate(stallId) ?: throw StallNotFoundException()
        stallCategoryRepository.deleteLinksByStallId(stallId)
        stallRepository.deleteById(stallId)
    }

    private fun requireStall(stallId: Long) {
        stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
    }
}
