package domain.repository

import domain.model.Supply
import domain.model.SupplyEnriched
import domain.model.SupplyProduct
import domain.model.SupplyProductDetail
import domain.model.SupplyStatus

interface SupplyRepository {
    // Supply
    suspend fun findAll(warehouseId: Int? = null, status: SupplyStatus? = null): List<SupplyEnriched>
    suspend fun findAllByUserId(userId: Int, warehouseId: Int? = null, status: SupplyStatus? = null): List<SupplyEnriched>
    suspend fun findById(id: Long): SupplyEnriched?
    suspend fun findByIdAndUserId(id: Long, userId: Int): SupplyEnriched?
    suspend fun create(supplierId: Int, warehouseId: Int, userId: Int): SupplyEnriched
    suspend fun updateStatus(id: Long, status: SupplyStatus): SupplyEnriched?
    suspend fun updateTotalPrice(id: Long): Unit
    suspend fun delete(id: Long): Boolean

    // Access control
    suspend fun hasAccess(supplyId: Long, userId: Int): Boolean

    // Supply products
    suspend fun findProducts(supplyId: Long): List<SupplyProduct>
    suspend fun findProductsDetails(supplyId: Long): List<SupplyProductDetail>
    suspend fun addProduct(supplyId: Long, productId: Long, quantity: Int, unitPrice: Long): SupplyProduct
    suspend fun removeProduct(supplyId: Long, productId: Long): Boolean

    suspend fun countByStatus(): Map<String, Int>
}