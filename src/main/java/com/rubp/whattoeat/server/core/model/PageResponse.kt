package com.rubp.whattoeat.server.core.model

data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long
) {
    init {
        require(size > 0) { "Page size must be positive" }
    }

    val totalPages: Int = if (total == 0L) 0 else ((total + size - 1) / size).toInt()
}
