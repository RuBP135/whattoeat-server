package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StallCategoryRepository : JpaRepository<StallCategoryEntry, Long> {
    fun findAllByOrderByIdAsc(): List<StallCategoryEntry>

    @Query(
        """
        select category
        from StallEntry stall
        join stall.categories category
        where stall.id = :stallId
        order by category.id
        """
    )
    fun findByStallId(@Param("stallId") stallId: Long): List<StallCategoryEntry>
}
