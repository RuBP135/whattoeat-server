package com.rubp.whattoeat.server.feature.location.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface LocationRepository : JpaRepository<LocationEntry, Long> {
    @Query(
        """
        select location
        from LocationEntry location
        order by location.ordinal, location.id
        """
    )
    override fun findAll(): List<LocationEntry>

    @Query(
        """
        select max(location.ordinal)
        from LocationEntry location
        where location.building.id = :buildingId
        """
    )
    fun findMaxOrdinalByBuildingId(@Param("buildingId") buildingId: Long): Int?

    @Query(
        value = """
        select name
        from location
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
        update location
        set name = :name
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateNameById(@Param("id") id: Long, @Param("name") name: String): Int

    @Query(
        value = """
        select id
        from location
        where building_id = :buildingId
        order by id
        for update
        """,
        nativeQuery = true
    )
    fun findIdsByBuildingIdForUpdate(@Param("buildingId") buildingId: Long): List<Long>

    @Modifying
    @Transactional
    @Query(
        value = """
        update location
        set ordinal = :ordinal
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateOrdinalById(@Param("id") id: Long, @Param("ordinal") ordinal: Int): Int
}
