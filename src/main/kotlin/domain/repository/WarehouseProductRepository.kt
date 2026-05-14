package domain.repository

import domain.model.Category
import domain.model.SupplyProduct
import domain.model.WarehouseProduct
import domain.model.WarehouseProductDetail

interface WarehouseProductRepository {
    suspend fun findByWarehouse(warehouseId: Int): List<WarehouseProduct>
    suspend fun findByWarehouseWithDetails(warehouseId: Int): List<WarehouseProductDetail>
    suspend fun findByWarehouseIdAndProductId(warehouseId: Int, productId: Long): WarehouseProduct?
    suspend fun findByWarehouseGroupedByCategory(warehouseId: Int): Map<Category, List<WarehouseProductDetail>>
    suspend fun upsert(warehouseId: Int, productId: Long, quantity: Int, unitPrice: Long): WarehouseProduct
    suspend fun getTotalStats(warehouseId: Int): WarehouseStats
    suspend fun upsertAll(warehouseId: Int, products: List<SupplyProduct>)
    suspend fun getTotalQuantityAll(): Int
    suspend fun getTotalPriceAll(): Long
}

data class WarehouseStats(
    val totalQuantity: Int,
    val totalPrice: Long,
    val uniqueProducts: Int
)