package com.rubp.whattoeat.server.feature.location.domain

import com.fasterxml.jackson.annotation.JsonProperty

enum class BuildingKind {
    @JsonProperty("canteen")
    CANTEEN,

    @JsonProperty("other")
    OTHER
}
