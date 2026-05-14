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

    suspend fun getAllWarehouses(): List<Warehouse> =
        repository.findAll()

    suspend fun getWarehouseById(id: Int): Warehouse =
        repository.findById(id)
            ?: throw NoSuchElementException("Warehouse $id not found")

    suspend fun createWarehouse(dto: CreateWarehouseRequest): Warehouse {
        if (dto.address.isBlank())
            throw IllegalArgumentException("Address must not be blank")

        return repository.create(
            dto.title?.trim(),
            dto.address.trim(),
            dto.capacity,
            dto.width,
            dto.length,
            dto.height
        )
    }

    suspend fun updateWarehouseById(id: Int, dto: UpdateWarehouseRequest): Warehouse {
        getWarehouseById(id)

        if (dto.address != null && dto.address.isBlank())
            throw IllegalArgumentException("Address must not be blank")

        return repository.update(
            id,
            dto.title?.trim(),
            dto.address?.trim(),
            dto.capacity,
            dto.width,
            dto.length,
            dto.height
        ) ?: throw NoSuchElementException("Warehouse $id not found")
    }

    suspend fun deleteWarehouseById(id: Int) {
        if (repository.hasProducts(id))
            throw IllegalArgumentException("Cannot delete warehouse with products")

        val deleted = repository.delete(id)
        if (!deleted) throw NoSuchElementException("Warehouse $id not found")
    }

    suspend fun getProductsByWarehouseId(warehouseId: Int): List<WarehouseProductDetail> {
        getWarehouseById(warehouseId)
        return warehouseProductRepository.findByWarehouseWithDetails(warehouseId)
    }

    suspend fun getProductsGroupedByCategory(warehouseId: Int): Map<Category, List<WarehouseProductDetail>> {
        getWarehouseById(warehouseId)
        return warehouseProductRepository.findByWarehouseGroupedByCategory(warehouseId)
    }

    suspend fun getWarehouseStats(warehouseId: Int): WarehouseStats {
        getWarehouseById(warehouseId)
        return warehouseProductRepository.getTotalStats(warehouseId)
    }

    suspend fun getWarehouseIdBySupplyId(supplyId: Long): Int =
        repository.findBySupplyId(supplyId)?.warehouseId
            ?: throw NoSuchElementException("Warehouse for supply $supplyId not found")
}