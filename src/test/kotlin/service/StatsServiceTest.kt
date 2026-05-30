package service

import api.dto.GlobalStatsResponse
import domain.repository.WarehouseStats
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StatsServiceTest : BaseServiceTest() {

    private lateinit var service: StatsService

    private fun initService() {
        service = StatsService(warehouseRepository, supplyRepository, warehouseProductRepository)
    }

    @Test
    fun `getGlobalStats should return aggregated stats`() = runTest {
        val warehouses = listOf(
            mockWarehouse(1, "Warehouse 1", "Address 1"),
            mockWarehouse(2, "Warehouse 2", "Address 2")
        )
        val suppliesByStatus = mapOf(
            "CREATED" to 5,
            "PENDING" to 3,
            "COMPLETED" to 10
        )

        coEvery { warehouseRepository.findAllByUserId(1) } returns warehouses
        coEvery { warehouseProductRepository.getTotalQuantityByUserId(1) } returns 500
        coEvery { warehouseProductRepository.getTotalPriceByUserId(1) } returns 100000L
        coEvery { supplyRepository.countByStatusByUserId(1) } returns suppliesByStatus
        initService()

        val result = service.getGlobalStats(1)

        assertEquals(2, result.totalWarehouses)
        assertEquals(500, result.totalQuantity)
        assertEquals(100000L, result.totalPrice)
        assertEquals(3, result.suppliesByStatus.size)
        assertEquals(5, result.suppliesByStatus["CREATED"])

        coVerify { warehouseRepository.findAllByUserId(1) }
        coVerify { warehouseProductRepository.getTotalQuantityByUserId(1) }
        coVerify { warehouseProductRepository.getTotalPriceByUserId(1) }
        coVerify { supplyRepository.countByStatusByUserId(1) }
    }

    @Test
    fun `getGlobalStats should return zero values when no data`() = runTest {
        coEvery { warehouseRepository.findAllByUserId(1) } returns emptyList()
        coEvery { warehouseProductRepository.getTotalQuantityByUserId(1) } returns 0
        coEvery { warehouseProductRepository.getTotalPriceByUserId(1) } returns 0L
        coEvery { supplyRepository.countByStatusByUserId(1) } returns emptyMap()
        initService()

        val result = service.getGlobalStats(1)

        assertEquals(0, result.totalWarehouses)
        assertEquals(0, result.totalQuantity)
        assertEquals(0L, result.totalPrice)
        assertEquals(0, result.suppliesByStatus.size)
    }

    @Test
    fun `getGlobalStats should handle empty supplies status map`() = runTest {
        val warehouses = listOf(mockWarehouse(1))
        coEvery { warehouseRepository.findAllByUserId(1) } returns warehouses
        coEvery { warehouseProductRepository.getTotalQuantityByUserId(1) } returns 100
        coEvery { warehouseProductRepository.getTotalPriceByUserId(1) } returns 5000L
        coEvery { supplyRepository.countByStatusByUserId(1) } returns emptyMap()
        initService()

        val result = service.getGlobalStats(1)

        assertEquals(1, result.totalWarehouses)
        assertEquals(100, result.totalQuantity)
        assertEquals(5000L, result.totalPrice)
        assertEquals(0, result.suppliesByStatus.size)
    }
}
