package com.rubp.whattoeat.server.feature.stall.model

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

class UpdateStallRequest(
    @field:Min(1) val slot: Int? = null,
    name: String? = null,
    val locationId: Long? = null
) {
    @Size(min = 1, max = 100) val name: String? = name?.trim()

    init {
        if (slot == null && this.name == null && locationId == null) {
            throw IllegalArgumentException("至少提供一个要修改的字段")
        }
    }
}
