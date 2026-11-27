package com.rubp.whattoeat.server.feature.dish.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface DishRepository : JpaRepository<DishEntry, Long> {
    @Query(
        """
        select dish
        from DishEntry dish
        where dish.stall.id = :stallId
        order by dish.id
        """
    )
    fun findByStallId(@Param("stallId") stallId: Long): List<DishEntry>

    @Query(
        value = """
        select name
        from dish
        where id = :id
        for update
        """,
        nativeQuery = true
    )
    fun findNameByIdForUpdate(@Param("id") id: Long): String?

    @Modifying
    @Transactional
    @Query(
        value = """
        update dish
        set name = :name
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateNameById(@Param("id") id: Long, @Param("name") name: String): Int
}
