package com.rubp.whattoeat.server.feature.location.model

import com.rubp.whattoeat.server.feature.location.domain.BuildingKind

data class BuildingResponse(
    val id: Long,
    val name: String,
    val kind: BuildingKind
)
