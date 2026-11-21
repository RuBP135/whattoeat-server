package com.rubp.whattoeat.server.feature.stall.service

import com.rubp.whattoeat.server.feature.stall.data.StallRepository
import com.rubp.whattoeat.server.feature.stall.model.StallResponse
import org.springframework.stereotype.Service

@Service
class StallService(
    private val stallRepository: StallRepository
) {
    fun listStalls(): List<StallResponse> {
        return stallRepository.findActiveStalls().map { stall ->
            StallResponse(stall.id, stall.name, stall.location.id)
        }
    }
}
