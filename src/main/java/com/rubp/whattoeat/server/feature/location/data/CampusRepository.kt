package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface CampusRepository : JpaRepository<CampusEntry, Long> {
    @Query(
        """
        select campus
        from CampusEntry campus
        order by campus.id
        """
    )
    override fun findAll(): List<CampusEntry>

    @Query(
        value = """
        select name
        from campus
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
        update campus
        set name = :name
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateNameById(@Param("id") id: Long, @Param("name") name: String): Int
}
