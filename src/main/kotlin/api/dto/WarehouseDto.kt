package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWarehouseRequest(
    val title: String? = null,
    val address: String,
    val capacity: Int? = null,
    val width: Long,
    val length: Long,
    val height: Long,
)

@Serializable
data class UpdateWarehouseRequest(
    val title: String? = null,
    val address: String? = null,
    val capacity: Int? = null,
    val width: Long? = null,
    val length: Long? = null,
    val height: Long? = null
)

@Serializable
data class WarehouseResponse(
    val warehouseId: Int,
    val title: String?,
    val address: String,
    val capacity: Int?,
    val width: Long,
    val length: Long,
    val height: Long
)

@Serializable
data class WarehouseCategoryGroupResponse(
    val categoryId: Int,
    val categoryTitle: String,
    val products: List<WarehouseProductDetailResponse>
)

@Serializable
data class WarehouseProductDetailResponse(
    val warehouseProductId: Long,
    val quantity: Int,
    val product: ProductResponse
)

@Serializable
data class GlobalStatsResponse(
    val totalWarehouses: Int,
    val totalQuantity: Int,
    val totalPrice: Long,
    val suppliesByStatus: Map<String, Int>
)