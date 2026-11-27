package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BuildingRepository : JpaRepository<BuildingEntry, Long> {
    @Query(
        """
        select building
        from BuildingEntry building
        order by building.ordinal, building.id
        """
    )
    override fun findAll(): List<BuildingEntry>
}
