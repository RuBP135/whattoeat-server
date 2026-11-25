package com.rubp.whattoeat.server.feature.stall.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

class CreateStallRequest(
    @field:Min(1) val slot: Int,
    name: String,
    val locationId: Long
) {
    @Size(min = 1, max = 100) val name: String = name.trim()
}
