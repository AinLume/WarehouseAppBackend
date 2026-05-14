package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSupplyRequest(
    val supplierId: Int,
    val warehouseId: Int
)

@Serializable
data class UpdateSupplyStatusRequest(
    val status: String
)

@Serializable
data class AddSupplyProductRequest(
    val productId: Long,
    val quantity: Int,
    val unitPrice: Long
)

@Serializable
data class SupplyResponse(
    val supplyId: Long,
    val supplierId: Int,
    val supplierName: String,
    val warehouseId: Int,
    val warehouseTitle: String?,
    val status: String,
    val totalPrice: Long,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class SupplyDetailResponse(
    val supplyId: Long,
    val supplierId: Int,
    val supplierName: String,
    val warehouseId: Int,
    val warehouseTitle: String?,
    val status: String,
    val totalPrice: Long,
    val createdAt: String,
    val updatedAt: String,
    val products: List<SupplyProductDetailResponse>
)

@Serializable
data class SupplyProductResponse(
    val supplyId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Long,
    val totalPrice: Long
)

@Serializable
data class SupplyProductDetailResponse(
    val supplyProductId: Long,
    val quantity: Int,
    val unitPrice: Long,
    val product: ProductResponse
)