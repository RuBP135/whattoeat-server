package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LocationRepository : JpaRepository<LocationEntry, Long> {
    @Query(
        """
        select location
        from LocationEntry location
        order by location.id
        """
    )
    override fun findAll(): List<LocationEntry>
}
