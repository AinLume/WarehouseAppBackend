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
    suspend fun getGlobalStats(userId: Int): GlobalStatsResponse {
        val totalWarehouses = warehouseRepository.findAllByUserId(userId).size
        val totalQuantity = warehouseProductRepository.getTotalQuantityByUserId(userId)
        val totalPrice = warehouseProductRepository.getTotalPriceByUserId(userId)
        val suppliesByStatus = supplyRepository.countByStatusByUserId(userId)

        return GlobalStatsResponse(
            totalWarehouses = totalWarehouses,
            totalQuantity = totalQuantity,
            totalPrice = totalPrice,
            suppliesByStatus = suppliesByStatus
        )
    }
}