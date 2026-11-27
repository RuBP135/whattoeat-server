package com.rubp.whattoeat.server.feature.dish.model

import jakarta.validation.constraints.Size

class DishRequest(name: String) {
    @Size(min = 1, max = 80) val name: String = name.trim()
}
