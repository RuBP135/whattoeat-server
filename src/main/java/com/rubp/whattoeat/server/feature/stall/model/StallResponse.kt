package com.rubp.whattoeat.server.feature.stall.model

data class StallResponse(
    val id: Long,
    val slot: Int,
    val name: String,
    val locationId: Long
)
