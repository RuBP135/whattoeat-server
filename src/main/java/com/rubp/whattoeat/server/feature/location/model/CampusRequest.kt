package com.rubp.whattoeat.server.feature.location.model

import jakarta.validation.constraints.Size

class CampusRequest(name: String) {
    @Size(min = 1, max = 50) val name: String = name.trim()
}
