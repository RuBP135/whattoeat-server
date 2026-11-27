package com.rubp.whattoeat.server.feature.location.model

import jakarta.validation.constraints.Size

class LocationRequest(name: String) {
    @Size(min = 1, max = 100) val name: String = name.trim()
}
