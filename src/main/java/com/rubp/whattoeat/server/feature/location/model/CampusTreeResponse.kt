package com.rubp.whattoeat.server.feature.location.model

data class CampusTreeResponse(
    val id: Long,
    val name: String,
    val buildings: List<BuildingTreeResponse>
)
