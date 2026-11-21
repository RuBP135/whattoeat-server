package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DishRepository : JpaRepository<DishEntry, Long> {
    @Query(
        """
        select dish
        from DishEntry dish
        where dish.stall.id = :stallId
        order by dish.name
        """
    )
    fun findByStallId(@Param("stallId") stallId: Long): List<DishEntry>
}
