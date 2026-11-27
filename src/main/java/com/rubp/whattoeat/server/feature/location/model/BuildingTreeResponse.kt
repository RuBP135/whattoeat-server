package com.rubp.whattoeat.server.feature.location.model

import com.rubp.whattoeat.server.feature.location.domain.BuildingKind

data class BuildingTreeResponse(
    val id: Long,
    val name: String,
    val kind: BuildingKind,
    val locations: List<LocationResponse>
)
