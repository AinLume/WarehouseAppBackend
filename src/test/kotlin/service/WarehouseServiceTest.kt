package service

import api.dto.CreateWarehouseRequest
import api.dto.UpdateWarehouseRequest
import domain.model.Category
import domain.model.Warehouse
import domain.model.WarehouseProductDetail
import domain.repository.WarehouseStats
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WarehouseServiceTest : BaseServiceTest() {

    private lateinit var service: WarehouseService

    private fun initService() {
        service = WarehouseService(warehouseRepository, warehouseProductRepository)
    }

    @Test
    fun `getAllWarehouses should return list of warehouses`() = runTest {
        val expected = listOf(
            mockWarehouse(1, "Main Warehouse", "123 Main St"),
            mockWarehouse(2, "Secondary Warehouse", "456 Oak Ave")
        )
        coEvery { warehouseRepository.findAllByUserId(1) } returns expected
        initService()

        val result = service.getAllWarehouses(1)

        assertEquals(2, result.size)
        assertEquals("Main Warehouse", result[0].title)
        coVerify { warehouseRepository.findAllByUserId(1) }
    }

    @Test
    fun `getWarehouseById should return warehouse when exists and belongs to user`() = runTest {
        val expected = mockWarehouse(1, "Main Warehouse")
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns expected
        initService()

        val result = service.getWarehouseById(1, 1)

        assertEquals("Main Warehouse", result.title)
        coVerify { warehouseRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `getWarehouseById should throw NoSuchElementException when not found`() = runTest {
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.getWarehouseById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `createWarehouse should create warehouse with valid data`() = runTest {
        val dto = CreateWarehouseRequest(
            title = "Main Warehouse",
            address = "123 Main St",
            capacity = 1000,
            width = 100L,
            length = 200L,
            height = 50L
        )
        val expected = mockWarehouse(1, "Main Warehouse", "123 Main St")
        coEvery {
            warehouseRepository.create("Main Warehouse", "123 Main St", 1000, 100L, 200L, 50L, 1)
        } returns expected
        initService()

        val result = service.createWarehouse(dto, 1)

        assertEquals("Main Warehouse", result.title)
        coVerify {
            warehouseRepository.create("Main Warehouse", "123 Main St", 1000, 100L, 200L, 50L, 1)
        }
    }

    @Test
    fun `createWarehouse should trim title and address`() = runTest {
        val dto = CreateWarehouseRequest(
            title = "  Main Warehouse  ",
            address = "  123 Main St  ",
            capacity = 1000,
            width = 100L,
            length = 200L,
            height = 50L
        )
        val expected = mockWarehouse(1, "Main Warehouse", "123 Main St")
        coEvery {
            warehouseRepository.create("Main Warehouse", "123 Main St", 1000, 100L, 200L, 50L, 1)
        } returns expected
        initService()

        val result = service.createWarehouse(dto, 1)

        assertEquals("Main Warehouse", result.title)
        coVerify {
            warehouseRepository.create("Main Warehouse", "123 Main St", 1000, 100L, 200L, 50L, 1)
        }
    }

    @Test
    fun `createWarehouse should throw IllegalArgumentException when address is blank`() = runTest {
        val dto = CreateWarehouseRequest(
            address = "   ",
            width = 100L,
            length = 200L,
            height = 50L
        )
        initService()

        try {
            service.createWarehouse(dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Address must not be blank", e.message)
        }
        coVerify(exactly = 0) { warehouseRepository.create(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateWarehouseById should update warehouse when exists`() = runTest {
        val dto = UpdateWarehouseRequest(
            title = "Updated Warehouse",
            address = "456 Oak Ave",
            capacity = 2000,
            width = 150L,
            length = 250L,
            height = 60L
        )
        val expected = mockWarehouse(1, "Updated Warehouse", "456 Oak Ave")
        coEvery {
            warehouseRepository.update(1, "Updated Warehouse", "456 Oak Ave", 2000, 150L, 250L, 60L, 1)
        } returns expected
        initService()

        val result = service.updateWarehouseById(1, dto, 1)

        assertEquals("Updated Warehouse", result.title)
        coVerify {
            warehouseRepository.update(1, "Updated Warehouse", "456 Oak Ave", 2000, 150L, 250L, 60L, 1)
        }
    }

    @Test
    fun `updateWarehouseById should throw IllegalArgumentException when address is blank`() = runTest {
        initService()

        try {
            service.updateWarehouseById(1, UpdateWarehouseRequest(address = "   "), 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Address must not be blank", e.message)
        }
        coVerify(exactly = 0) { warehouseRepository.update(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateWarehouseById should throw NoSuchElementException when not found`() = runTest {
        val dto = UpdateWarehouseRequest(title = "New Title")
        coEvery {
            warehouseRepository.update(1, "New Title", null, null, null, null, null, 1)
        } returns null
        initService()

        try {
            service.updateWarehouseById(1, dto, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `deleteWarehouseById should delete warehouse when exists and has no products`() = runTest {
        coEvery { warehouseRepository.hasAccess(1, 1) } returns true
        coEvery { warehouseRepository.hasProducts(1) } returns false
        coEvery { warehouseRepository.delete(1, 1) } returns true
        initService()

        service.deleteWarehouseById(1, 1)

        coVerify { warehouseRepository.hasAccess(1, 1) }
        coVerify { warehouseRepository.hasProducts(1) }
        coVerify { warehouseRepository.delete(1, 1) }
    }

    @Test
    fun `deleteWarehouseById should throw NoSuchElementException when no access`() = runTest {
        coEvery { warehouseRepository.hasAccess(1, 1) } returns false
        initService()

        try {
            service.deleteWarehouseById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
        coVerify(exactly = 0) { warehouseRepository.delete(any(), any()) }
    }

    @Test
    fun `deleteWarehouseById should throw IllegalArgumentException when has products`() = runTest {
        coEvery { warehouseRepository.hasAccess(1, 1) } returns true
        coEvery { warehouseRepository.hasProducts(1) } returns true
        initService()

        try {
            service.deleteWarehouseById(1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot delete warehouse with products", e.message)
        }
        coVerify(exactly = 0) { warehouseRepository.delete(any(), any()) }
    }

    @Test
    fun `getProductsByWarehouseId should return products list`() = runTest {
        val warehouse = mockWarehouse(1, "Main Warehouse")
        val products = listOf(
            mockWarehouseProductDetail(1, 10, mockProduct(1)),
            mockWarehouseProductDetail(2, 20, mockProduct(2))
        )
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns warehouse
        coEvery { warehouseProductRepository.findByWarehouseWithDetails(1) } returns products
        initService()

        val result = service.getProductsByWarehouseId(1, 1)

        assertEquals(2, result.size)
        coVerify { warehouseRepository.findByIdAndUserId(1, 1) }
        coVerify { warehouseProductRepository.findByWarehouseWithDetails(1) }
    }

    @Test
    fun `getProductsByWarehouseId should throw NoSuchElementException when warehouse not found`() = runTest {
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.getProductsByWarehouseId(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `getProductsGroupedByCategory should return grouped products`() = runTest {
        val warehouse = mockWarehouse(1, "Main Warehouse")
        val electronicsCategory = Category(1, "Electronics")
        val products = listOf(mockWarehouseProductDetail(1, 10, mockProduct(1)))
        val groupedMap = mapOf(electronicsCategory to products)
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns warehouse
        coEvery { warehouseProductRepository.findByWarehouseGroupedByCategory(1) } returns groupedMap
        initService()

        val result = service.getProductsGroupedByCategory(1, 1)

        assertEquals(1, result.size)
        coVerify { warehouseRepository.findByIdAndUserId(1, 1) }
        coVerify { warehouseProductRepository.findByWarehouseGroupedByCategory(1) }
    }

    @Test
    fun `getWarehouseStats should return stats`() = runTest {
        val warehouse = mockWarehouse(1, "Main Warehouse")
        val stats = mockWarehouseStats(100, 50000L, 10)
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns warehouse
        coEvery { warehouseProductRepository.getTotalStats(1) } returns stats
        initService()

        val result = service.getWarehouseStats(1, 1)

        assertEquals(100, result.totalQuantity)
        assertEquals(50000L, result.totalPrice)
        coVerify { warehouseRepository.findByIdAndUserId(1, 1) }
        coVerify { warehouseProductRepository.getTotalStats(1) }
    }

    @Test
    fun `getWarehouseIdBySupplyId should return warehouse id when exists and user has access`() = runTest {
        val warehouseId = 5
        coEvery { warehouseRepository.findBySupplyId(1) } returns mockWarehouse(warehouseId)
        coEvery { warehouseRepository.hasAccess(warehouseId, 1) } returns true
        initService()

        val result = service.getWarehouseIdBySupplyId(1, 1)

        assertEquals(warehouseId, result)
        coVerify { warehouseRepository.findBySupplyId(1) }
        coVerify { warehouseRepository.hasAccess(warehouseId, 1) }
    }

    @Test
    fun `getWarehouseIdBySupplyId should throw NoSuchElementException when warehouse not found`() = runTest {
        coEvery { warehouseRepository.findBySupplyId(1) } returns null
        initService()

        try {
            service.getWarehouseIdBySupplyId(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `getWarehouseIdBySupplyId should throw NoSuchElementException when no access`() = runTest {
        val warehouseId = 5
        coEvery { warehouseRepository.findBySupplyId(1) } returns mockWarehouse(warehouseId)
        coEvery { warehouseRepository.hasAccess(warehouseId, 1) } returns false
        initService()

        try {
            service.getWarehouseIdBySupplyId(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `hasAccess should return true when user has access`() = runTest {
        coEvery { warehouseRepository.hasAccess(1, 1) } returns true
        initService()

        val result = service.hasAccess(1, 1)

        assertEquals(true, result)
    }

    @Test
    fun `hasAccess should return false when user has no access`() = runTest {
        coEvery { warehouseRepository.hasAccess(1, 2) } returns false
        initService()

        val result = service.hasAccess(1, 2)

        assertEquals(false, result)
    }

    @Test
    fun `getWarehouseByIdInternal should return warehouse when exists`() = runTest {
        val expected = mockWarehouse(1, "Main Warehouse")
        coEvery { warehouseRepository.findById(1) } returns expected
        initService()

        val result = service.getWarehouseByIdInternal(1)

        assertEquals("Main Warehouse", result.title)
        coVerify { warehouseRepository.findById(1) }
    }

    @Test
    fun `getWarehouseByIdInternal should throw NoSuchElementException when not found`() = runTest {
        coEvery { warehouseRepository.findById(1) } returns null
        initService()

        try {
            service.getWarehouseByIdInternal(1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `getWarehouseIdBySupplyIdInternal should return warehouse id`() = runTest {
        coEvery { warehouseRepository.findBySupplyId(1) } returns mockWarehouse(5)
        initService()

        val result = service.getWarehouseIdBySupplyIdInternal(1)

        assertEquals(5, result)
    }

    @Test
    fun `getWarehouseIdBySupplyIdInternal should throw NoSuchElementException when not found`() = runTest {
        coEvery { warehouseRepository.findBySupplyId(1) } returns null
        initService()

        try {
            service.getWarehouseIdBySupplyIdInternal(1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }
}
