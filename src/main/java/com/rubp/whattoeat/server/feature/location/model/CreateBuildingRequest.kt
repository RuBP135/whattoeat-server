package com.rubp.whattoeat.server.feature.location.model

import com.rubp.whattoeat.server.feature.location.domain.BuildingKind
import jakarta.validation.constraints.Size

class CreateBuildingRequest(name: String, val kind: BuildingKind) {
    @Size(min = 1, max = 100) val name: String = name.trim()
}
