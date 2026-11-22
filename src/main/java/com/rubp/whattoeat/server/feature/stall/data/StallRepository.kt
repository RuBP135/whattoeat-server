package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

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

    @Query(
        """
        select stall
        from StallEntry stall
        join fetch stall.location
        join stall.categories category
        where stall.active = true
          and category.id = :categoryId
        order by stall.location.id, stall.name
        """
    )
    fun findActiveStallsByCategoryId(@Param("categoryId") categoryId: Long): List<StallEntry>
}
