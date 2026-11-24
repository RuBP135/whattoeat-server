package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

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

    @Modifying
    @Transactional
    @Query(
        value = """
        update stall_category
        set name = :name
        where id = :categoryId
        """,
        nativeQuery = true
    )
    fun updateName(@Param("categoryId") categoryId: Long, @Param("name") name: String): Int

    @Modifying
    @Query(
        value = """
        delete from stall_category_link
        where category_id = :categoryId
        """,
        nativeQuery = true
    )
    fun deleteStallLinksByCategoryId(@Param("categoryId") categoryId: Long): Int

    @Modifying
    @Query(
        value = """
        delete from stall_category
        where id = :categoryId
        """,
        nativeQuery = true
    )
    fun deleteCategoryById(@Param("categoryId") categoryId: Long): Int

    @Modifying
    @Query(
        value = """
        delete from stall_category_link
        where stall_id = :stallId
        """,
        nativeQuery = true
    )
    fun deleteLinksByStallId(@Param("stallId") stallId: Long): Int

    @Modifying
    @Query(
        value = """
        insert into stall_category_link (stall_id, category_id)
        select :stallId, id
        from stall_category
        where id in (:categoryIds)
        """,
        nativeQuery = true
    )
    fun insertLinks(
        @Param("stallId") stallId: Long,
        @Param("categoryIds") categoryIds: Set<Long>
    ): Int
}
