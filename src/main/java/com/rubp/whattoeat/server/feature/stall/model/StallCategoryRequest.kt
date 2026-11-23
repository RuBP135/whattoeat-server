package com.rubp.whattoeat.server.feature.stall.model

import jakarta.validation.constraints.Size

class StallCategoryRequest(name: String) {
    @Size(min = 1, max = 20) val name: String = name.trim()
}
