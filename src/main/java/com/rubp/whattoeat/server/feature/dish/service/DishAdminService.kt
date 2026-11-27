package com.rubp.whattoeat.server.feature.dish.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import com.rubp.whattoeat.server.feature.dish.data.DishEntry
import com.rubp.whattoeat.server.feature.dish.data.DishRepository
import com.rubp.whattoeat.server.feature.dish.exception.DishAlreadyExistsException
import com.rubp.whattoeat.server.feature.dish.exception.DishNotFoundException
import com.rubp.whattoeat.server.feature.dish.model.DishRequest
import com.rubp.whattoeat.server.feature.dish.model.DishResponse
import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.exception.StallNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DishAdminService(
    private val dishRepository: DishRepository,
    private val stallRepository: StallRepository,
    private val adminAuditService: AdminAuditService
) {
    @Transactional(readOnly = true)
    fun listDishesByStallId(stallId: Long): List<DishResponse> {
        stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }

        return dishRepository.findByStallId(stallId).map { dish ->
            DishResponse(dish.id, dish.name)
        }
    }

    @Transactional
    fun createDish(stallId: Long, request: DishRequest): DishResponse {
        val stall = stallRepository.findById(stallId).orElseThrow { StallNotFoundException() }
        val dish = try {
            dishRepository.save(DishEntry(name = request.name, stall = stall))
        } catch (exception: DataIntegrityViolationException) {
            throw dishWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.CREATE,
            AuditResourceType.DISH,
            dish.id,
            "audit.dish.create",
            dish.name
        )
        return DishResponse(dish.id, dish.name)
    }

    @Transactional
    fun renameDish(dishId: Long, request: DishRequest) {
        val oldName = dishRepository.findNameByIdForUpdate(dishId)
            ?: throw DishNotFoundException()
        if (oldName == request.name) {
            return
        }
        try {
            dishRepository.updateNameById(dishId, request.name)
        } catch (exception: DataIntegrityViolationException) {
            throw dishWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.DISH,
            dishId,
            "audit.dish.rename",
            oldName,
            request.name
        )
    }

    private fun dishWriteFailure(exception: DataIntegrityViolationException): RuntimeException {
        return if (exception.isUniqueViolation()) {
            DishAlreadyExistsException()
        } else {
            exception
        }
    }
}
