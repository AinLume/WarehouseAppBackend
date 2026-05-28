package service

import api.dto.CreateWarehouseRequest
import api.dto.UpdateWarehouseRequest
import domain.model.Category
import domain.model.Warehouse
import domain.model.WarehouseProductDetail
import domain.repository.WarehouseProductRepository
import domain.repository.WarehouseRepository
import domain.repository.WarehouseStats

class WarehouseService(
    private val repository: WarehouseRepository,
    private val warehouseProductRepository: WarehouseProductRepository
) {

    // ============== Public methods (with userId check) ==============

    suspend fun getAllWarehouses(userId: Int): List<Warehouse> =
        repository.findAllByUserId(userId)

    suspend fun getWarehouseById(id: Int, userId: Int): Warehouse =
        repository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Warehouse $id not found")

    suspend fun createWarehouse(dto: CreateWarehouseRequest, userId: Int): Warehouse {
        if (dto.address.isBlank())
            throw IllegalArgumentException("Address must not be blank")

        return repository.create(
            dto.title?.trim(),
            dto.address.trim(),
            dto.capacity,
            dto.width,
            dto.length,
            dto.height,
            userId
        )
    }

    suspend fun updateWarehouseById(id: Int, dto: UpdateWarehouseRequest, userId: Int): Warehouse {
        if (dto.address != null && dto.address.isBlank())
            throw IllegalArgumentException("Address must not be blank")

        return repository.update(
            id,
            dto.title?.trim(),
            dto.address?.trim(),
            dto.capacity,
            dto.width,
            dto.length,
            dto.height,
            userId
        ) ?: throw NoSuchElementException("Warehouse $id not found")
    }

    suspend fun deleteWarehouseById(id: Int, userId: Int) {
        if (!repository.hasAccess(id, userId))
            throw NoSuchElementException("Warehouse $id not found")

        if (repository.hasProducts(id))
            throw IllegalArgumentException("Cannot delete warehouse with products")

        val deleted = repository.delete(id, userId)
        if (!deleted) throw NoSuchElementException("Warehouse $id not found")
    }

    suspend fun getProductsByWarehouseId(warehouseId: Int, userId: Int): List<WarehouseProductDetail> {
        getWarehouseById(warehouseId, userId)
        return warehouseProductRepository.findByWarehouseWithDetails(warehouseId)
    }

    suspend fun getProductsGroupedByCategory(warehouseId: Int, userId: Int): Map<Category, List<WarehouseProductDetail>> {
        getWarehouseById(warehouseId, userId)
        return warehouseProductRepository.findByWarehouseGroupedByCategory(warehouseId)
    }

    suspend fun getWarehouseStats(warehouseId: Int, userId: Int): WarehouseStats {
        getWarehouseById(warehouseId, userId)
        return warehouseProductRepository.getTotalStats(warehouseId)
    }

    suspend fun getWarehouseIdBySupplyId(supplyId: Long, userId: Int): Int {
        val warehouse = repository.findBySupplyId(supplyId)
            ?: throw NoSuchElementException("Warehouse for supply $supplyId not found")

        if (!repository.hasAccess(warehouse.warehouseId, userId))
            throw NoSuchElementException("Warehouse ${warehouse.warehouseId} not found")

        return warehouse.warehouseId
    }

    suspend fun hasAccess(warehouseId: Int, userId: Int): Boolean =
        repository.hasAccess(warehouseId, userId)


    // Внутренние методы без проверки userId
    suspend fun getWarehouseByIdInternal(id: Int): Warehouse =
        repository.findById(id)
            ?: throw NoSuchElementException("Warehouse $id not found")

    suspend fun getWarehouseIdBySupplyIdInternal(supplyId: Long): Int =
        repository.findBySupplyId(supplyId)?.warehouseId
            ?: throw NoSuchElementException("Warehouse for supply $supplyId not found")
}