package com.rubp.whattoeat.server.feature.location.model

import com.rubp.whattoeat.server.feature.location.domain.LocationKind

data class LocationResponse(
    val id: Long,
    val name: String,
    val kind: LocationKind
)
