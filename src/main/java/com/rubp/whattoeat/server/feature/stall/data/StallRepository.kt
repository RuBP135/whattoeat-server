package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StallRepository : JpaRepository<StallEntry, Long> {
    @Query(
        """
        select stall
        from StallEntry stall
        join fetch stall.location
        where stall.active = true
        order by stall.location.id, stall.name
        """
    )
    fun findActiveStalls(): List<StallEntry>
}
