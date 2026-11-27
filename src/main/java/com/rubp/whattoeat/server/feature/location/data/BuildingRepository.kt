package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface BuildingRepository : JpaRepository<BuildingEntry, Long> {
    @Query(
        """
        select building
        from BuildingEntry building
        order by building.ordinal, building.id
        """
    )
    override fun findAll(): List<BuildingEntry>

    @Query(
        """
        select max(building.ordinal)
        from BuildingEntry building
        where building.campus.id = :campusId
        """
    )
    fun findMaxOrdinalByCampusId(@Param("campusId") campusId: Long): Int?

    @Query(
        value = """
        select name
        from building
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
        update building
        set name = coalesce(:name, name),
            kind = coalesce(:kind, kind)
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateById(
        @Param("id") id: Long,
        @Param("name") name: String?,
        @Param("kind") kind: String?
    ): Int

    @Query(
        value = """
        select id
        from building
        where campus_id = :campusId
        order by id
        for update
        """,
        nativeQuery = true
    )
    fun findIdsByCampusIdForUpdate(@Param("campusId") campusId: Long): List<Long>

    @Modifying
    @Transactional
    @Query(
        value = """
        update building
        set ordinal = :ordinal
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateOrdinalById(@Param("id") id: Long, @Param("ordinal") ordinal: Int): Int
}
