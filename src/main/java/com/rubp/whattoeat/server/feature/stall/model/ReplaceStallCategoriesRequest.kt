package com.rubp.whattoeat.server.feature.stall.model

data class ReplaceStallCategoriesRequest(
    val categoryIds: Set<Long>
) {
    init {
        if (categoryIds.any { categoryId: Long? -> categoryId == null }) {
            throw IllegalArgumentException("分类 ID 不能为空")
        }
    }
}
