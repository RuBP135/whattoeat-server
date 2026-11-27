package com.rubp.whattoeat.server.feature.location.model

import com.rubp.whattoeat.server.feature.location.domain.BuildingKind
import jakarta.validation.constraints.Size

class UpdateBuildingRequest(
    name: String? = null,
    val kind: BuildingKind? = null
) {
    @Size(min = 1, max = 100) val name: String? = name?.trim()

    init {
        if (this.name == null && kind == null) {
            throw IllegalArgumentException("至少提供一个要修改的字段")
        }
    }
}
