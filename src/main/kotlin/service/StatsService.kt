package service

import api.dto.GlobalStatsResponse
import domain.repository.SupplyRepository
import domain.repository.WarehouseProductRepository
import domain.repository.WarehouseRepository

class StatsService(
    private val warehouseRepository: WarehouseRepository,
    private val supplyRepository: SupplyRepository,
    private val warehouseProductRepository: WarehouseProductRepository
) {
    suspend fun getGlobalStats(): GlobalStatsResponse {
        val totalWarehouses = warehouseRepository.findAll().size
        val totalQuantity = warehouseProductRepository.getTotalQuantityAll()
        val totalPrice = warehouseProductRepository.getTotalPriceAll()
        val suppliesByStatus = supplyRepository.countByStatus()

        return GlobalStatsResponse(
            totalWarehouses = totalWarehouses,
            totalQuantity = totalQuantity,
            totalPrice = totalPrice,
            suppliesByStatus = suppliesByStatus
        )
    }
}