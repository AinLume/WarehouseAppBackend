package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val title: String
)

@Serializable
data class UpdateCategoryRequest(
    val title: String
)

@Serializable
data class CategoryResponse(
    val categoryId: Int,
    val title: String
)