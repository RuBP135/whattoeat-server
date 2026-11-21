package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository

interface LocationRepository : JpaRepository<LocationEntry, Long> {
    fun findAllByOrderByIdAsc(): List<LocationEntry>
}
