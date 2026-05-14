package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
    val categoryId: Int,
    val title: String,
    val description: String? = null,
    val width: Int,
    val length: Int,
    val height: Int,
)

@Serializable
data class UpdateProductRequest(
    val categoryId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val width: Int?,
    val length: Int?,
    val height: Int?,
)

@Serializable
data class ProductResponse(
    val productId: Long,
    val categoryId: Int,
    val title: String,
    val description: String?,
    val width: Int,
    val length: Int,
    val height: Int,
    val volume: Double,
)

@Serializable
data class ProductExResponse(
    val productId: Long,
    val categoryId: Int,
    val categoryTitle: String,
    val title: String,
    val description: String?,
    val width: Int,
    val length: Int,
    val height: Int,
    val volume: Double,
)