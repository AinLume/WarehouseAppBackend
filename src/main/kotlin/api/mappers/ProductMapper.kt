package api.mappers

import api.dto.ProductExResponse
import api.dto.ProductResponse
import domain.model.Product
import domain.model.ProductEx

fun Product.toResponse() = ProductResponse(
    productId = productId,
    categoryId = categoryId,
    title = title,
    description = description,
    width = width,
    length = length,
    height = height,
    volume = (width * length * height)/1_000_000.0
)

fun ProductEx.toResponse() = ProductExResponse(
    productId = productId,
    categoryId = categoryId,
    categoryTitle = categoryTitle,
    title = title,
    description = description,
    width = width,
    length = length,
    height = height,
    volume = (width * length * height) / 1_000_000.0
)