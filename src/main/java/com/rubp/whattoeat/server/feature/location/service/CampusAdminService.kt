package com.rubp.whattoeat.server.feature.location.service

import com.rubp.whattoeat.server.core.data.isUniqueViolation
import com.rubp.whattoeat.server.feature.audit.domain.AuditOperation
import com.rubp.whattoeat.server.feature.audit.domain.AuditResourceType
import com.rubp.whattoeat.server.feature.audit.service.AdminAuditService
import com.rubp.whattoeat.server.feature.location.data.CampusEntry
import com.rubp.whattoeat.server.feature.location.data.CampusRepository
import com.rubp.whattoeat.server.feature.location.exception.CampusAlreadyExistsException
import com.rubp.whattoeat.server.feature.location.exception.CampusNotFoundException
import com.rubp.whattoeat.server.feature.location.model.CampusRequest
import com.rubp.whattoeat.server.feature.location.model.CampusResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CampusAdminService(
    private val campusRepository: CampusRepository,
    private val adminAuditService: AdminAuditService
) {
    @Transactional
    fun createCampus(request: CampusRequest): CampusResponse {
        val campus = try {
            campusRepository.save(CampusEntry(name = request.name))
        } catch (exception: DataIntegrityViolationException) {
            throw campusWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.CREATE,
            AuditResourceType.CAMPUS,
            campus.id,
            "audit.campus.create",
            campus.name
        )
        return CampusResponse(campus.id, campus.name)
    }

    @Transactional
    fun renameCampus(campusId: Long, request: CampusRequest) {
        val oldName = campusRepository.findNameByIdForUpdate(campusId)
            ?: throw CampusNotFoundException()
        if (oldName == request.name) {
            return
        }
        try {
            campusRepository.updateNameById(campusId, request.name)
        } catch (exception: DataIntegrityViolationException) {
            throw campusWriteFailure(exception)
        }
        adminAuditService.record(
            AuditOperation.UPDATE,
            AuditResourceType.CAMPUS,
            campusId,
            "audit.campus.rename",
            oldName,
            request.name
        )
    }

    private fun campusWriteFailure(exception: DataIntegrityViolationException): RuntimeException {
        return if (exception.isUniqueViolation()) {
            CampusAlreadyExistsException()
        } else {
            exception
        }
    }
}
