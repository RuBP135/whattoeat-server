package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CampusRepository : JpaRepository<CampusEntry, Long> {
    @Query(
        """
        select campus
        from CampusEntry campus
        order by campus.id
        """
    )
    override fun findAll(): List<CampusEntry>
}
