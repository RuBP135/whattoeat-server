package com.rubp.whattoeat.server.feature.stall.data

import org.springframework.data.jpa.repository.JpaRepository

interface StallCategoryRepository : JpaRepository<StallCategoryEntry, Long> {
    fun findAllByOrderByIdAsc(): List<StallCategoryEntry>
}
