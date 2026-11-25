package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

interface StallRepository : JpaRepository<StallEntry, Long> {
    @Query(
        """
            select stall
            from StallEntry stall
            join fetch stall.location location
            where (:locationId is null or location.id = :locationId)
            and (:active is null or stall.active = :active)
            and (:categoryId is null or exists (
                select 1
                from stall.categories category
                where category.id = :categoryId
            ))
            order by location.id, stall.slot, stall.id
        """
    )
    fun findAll(
        @Param("locationId") locationId: Long?,
        @Param("categoryId") categoryId: Long?,
        @Param("active") active: Boolean?
    ): List<StallEntry>

    @Query(
        """
        select stall
        from StallEntry stall
        join fetch stall.location
        where stall.id = :id
        """
    )
    override fun findById(@Param("id") id: Long): Optional<StallEntry>

    @Modifying
    @Transactional
    @Query(
        value = """
        update stall
        set slot = coalesce(:slot, slot),
            name = coalesce(:name, name),
            location_id = coalesce(:locationId, location_id)
        where id = :id
        """,
        nativeQuery = true
    )
    fun updateById(
        @Param("id") id: Long,
        @Param("slot") slot: Int?,
        @Param("name") name: String?,
        @Param("locationId") locationId: Long?
    ): Int

    @Query(
        value = """
        select id
        from stall
        where id = :id
        for update
        """,
        nativeQuery = true
    )
    fun findIdForUpdate(@Param("id") id: Long): Long?
}
