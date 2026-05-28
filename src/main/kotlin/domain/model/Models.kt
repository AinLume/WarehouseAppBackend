package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val categoryId: Int,
    val title: String
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String
)

enum class UserRole(val displayName: String) {
    ADMIN("Owner"),
    MANAGER("Manager"),
    WORKER("Worker");

    companion object {
        fun fromString(role: String): UserRole {
            return entries.find { it.name.equals(role, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown role: $role")
        }
    }
}

/**
 * Длинна, ширина и высота в сантиметрах
 * */
@Serializable
data class Product(
    val productId: Long,
    val categoryId: Int,
    val title: String,
    val description: String?,
    val width: Int,
    val length: Int,
    val height: Int
)

@Serializable
data class ProductEx(
    val productId: Long,
    val categoryId: Int,
    val categoryTitle: String,
    val title: String,
    val description: String?,
    val width: Int,
    val length: Int,
    val height: Int
)

@Serializable
data class Supplier(
    val supplierId: Int,
    val name: String,
    val phone: String,
    val email: String?,
    val address: String?
)

/**
 * Длинна, ширина и высота в метрах
 * */
@Serializable
data class Warehouse(
    val warehouseId: Int,
    val title: String?,
    val address: String,
    val capacity: Int?,
    val width: Long,
    val length: Long,
    val height: Long
)

enum class SupplyStatus {
    CREATED, PENDING, CONFIRMED, SHIPPED, DELIVERED, COMPLETED, CANCELLED;

    fun allowedTransitions(): List<SupplyStatus> = when (this) {
        CREATED -> listOf(PENDING, CANCELLED)
        PENDING -> listOf(CONFIRMED, CANCELLED)
        CONFIRMED -> listOf(SHIPPED, CANCELLED)
        SHIPPED -> listOf(DELIVERED, CANCELLED)
        DELIVERED -> listOf(COMPLETED, CANCELLED)
        COMPLETED -> emptyList()
        CANCELLED -> emptyList()
    }
}

@Serializable
data class Supply(
    val supplyId: Long,
    val supplierId: Int,
    val status: SupplyStatus,
    val totalPrice: Long,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class SupplyProduct(
    val supplyProductId: Long,
    val supplyId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Long
)

@Serializable
data class SupplyProductDetail(
    val supplyProductId: Long,
    val quantity: Int,
    val unitPrice: Long,
    val product: Product
)

data class SupplyEnriched(
    val supply: Supply,
    val warehouseId: Int,
    val warehouseTitle: String?,
    val supplierName: String
)


@Serializable
data class WarehouseProduct(
    val warehouseProductId: Long,
    val warehouseId: Int,
    val productId: Long,
    val quantity: Int,
    val totalPrice: Long
)

@Serializable
data class WarehouseProductDetail(
    val warehouseProductId: Long,
    val quantity: Int,
    val product: Product
)