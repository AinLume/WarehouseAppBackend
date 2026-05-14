package api.mappers

import api.dto.CategoryResponse
import domain.model.Category

fun Category.toResponse() = CategoryResponse(
    categoryId = categoryId,
    title = title
)