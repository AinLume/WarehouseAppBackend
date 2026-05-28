package domain.repository

import domain.model.Warehouse

interface WarehouseRepository {
    suspend fun findAll(): List<Warehouse>
    suspend fun findById(id: Int): Warehouse?
    suspend fun findByIdAndUserId(id: Int, userId: Int): Warehouse?
    suspend fun findAllByUserId(userId: Int): List<Warehouse>
    suspend fun create(
        title: String?,
        address: String,
        capacity: Int?,
        width: Long,
        length: Long,
        height: Long,
        userId: Int
    ): Warehouse
    suspend fun update(
        id: Int,
        title: String?,
        address: String?,
        capacity: Int?,
        width: Long?,
        length: Long?,
        height: Long?,
        userId: Int
    ): Warehouse?
    suspend fun delete(id: Int, userId: Int): Boolean
    suspend fun hasProducts(id: Int): Boolean
    suspend fun findBySupplyId(supplyId: Long): Warehouse?
    suspend fun hasAccess(warehouseId: Int, userId: Int): Boolean
}