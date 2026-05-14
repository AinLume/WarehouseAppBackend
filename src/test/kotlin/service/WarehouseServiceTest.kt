package service

import domain.repository.WarehouseStats
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertNull

class WarehouseServiceTest : BaseServiceTest() {

    // getAllWarehouses
    @Test
    fun getAllWarehouses_shouldReturnAllWarehouses() = runTest {
        coEvery { warehouseRepository.findAll() } returns listOf(testWarehouse)

        val result = warehouseService.getAllWarehouses()

        assertEquals(1, result.size)
        assertEquals(testWarehouse, result[0])
        coVerify(exactly = 1) { warehouseRepository.findAll() }
    }

    @Test
    fun getAllWarehouses_shouldReturnEmptyList() = runTest {
        coEvery { warehouseRepository.findAll() } returns emptyList()

        val result = warehouseService.getAllWarehouses()

        assertTrue(result.isEmpty())
    }

    
    // getWarehouseById
    @Test
    fun getWarehouseById_shouldReturnWarehouse() = runTest {
        coEvery { warehouseRepository.findById(1) } returns testWarehouse

        val result = warehouseService.getWarehouseById(1)

        assertEquals(testWarehouse, result)
    }

    @Test
    fun getWarehouseById_shouldThrowWhenNotFound() = runTest {
        coEvery { warehouseRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            warehouseService.getWarehouseById(99)
        }
    }

    
    // createWarehouse
    @Test
    fun createWarehouse_shouldReturnCreatedWarehouse() = runTest {
        coEvery {
            warehouseRepository.create("Главный склад", "Москва, ул. Складская, 5")
        } returns testWarehouse

        val result = warehouseService.createWarehouse(createWarehouseRequest)

        assertEquals(testWarehouse, result)
        coVerify(exactly = 1) {
            warehouseRepository.create("Главный склад", "Москва, ул. Складская, 5")
        }
    }

    @Test
    fun createWarehouse_shouldThrowWhenAddressIsBlank() = runTest {
        val request = createWarehouseRequest.copy(address = "   ")

        assertFailsWith<IllegalArgumentException> {
            warehouseService.createWarehouse(request)
        }
        coVerify(exactly = 0) { warehouseRepository.create(any(), any()) }
    }

    @Test
    fun createWarehouse_shouldCreateWithNullTitle() = runTest {
        val request = createWarehouseRequest.copy(title = null)
        val warehouseNoTitle = testWarehouse.copy(title = null)
        coEvery {
            warehouseRepository.create(null, "Москва, ул. Складская, 5")
        } returns warehouseNoTitle

        val result = warehouseService.createWarehouse(request)

        assertNull(result.title)
    }

    @Test
    fun createWarehouse_shouldTrimAddressBeforeSaving() = runTest {
        val request = createWarehouseRequest.copy(address = "  Москва, ул. Складская, 5  ")
        coEvery {
            warehouseRepository.create(any(), "Москва, ул. Складская, 5")
        } returns testWarehouse

        warehouseService.createWarehouse(request)

        coVerify { warehouseRepository.create(any(), "Москва, ул. Складская, 5") }
    }

    
    // updateWarehouseById
    @Test
    fun updateWarehouseById_shouldReturnUpdatedWarehouse() = runTest {
        val updated = testWarehouse.copy(title = "Новое название")
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery {
            warehouseRepository.update(1, "Новое название", null)
        } returns updated

        val result = warehouseService.updateWarehouseById(1, updateWarehouseRequest)

        assertEquals("Новое название", result.title)
        coVerify(exactly = 1) { warehouseRepository.update(1, "Новое название", null) }
    }

    @Test
    fun updateWarehouseById_shouldThrowWhenNotFound() = runTest {
        coEvery { warehouseRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            warehouseService.updateWarehouseById(99, updateWarehouseRequest)
        }
        coVerify(exactly = 0) { warehouseRepository.update(any(), any(), any()) }
    }

    @Test
    fun updateWarehouseById_shouldThrowWhenAddressIsBlank() = runTest {
        val request = updateWarehouseRequest.copy(address = "   ")
        coEvery { warehouseRepository.findById(1) } returns testWarehouse

        assertFailsWith<IllegalArgumentException> {
            warehouseService.updateWarehouseById(1, request)
        }
        coVerify(exactly = 0) { warehouseRepository.update(any(), any(), any()) }
    }

    @Test
    fun updateWarehouseById_shouldUpdateOnlyProvidedFields() = runTest {
        val request = updateWarehouseRequest.copy(title = null, address = "Новый адрес")
        val updated = testWarehouse.copy(address = "Новый адрес")
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery { warehouseRepository.update(1, null, "Новый адрес") } returns updated

        val result = warehouseService.updateWarehouseById(1, request)

        assertEquals("Новый адрес", result.address)
        assertEquals(testWarehouse.title, result.title) // title не изменился
    }

    
    // deleteWarehouseById
    @Test
    fun deleteWarehouseById_shouldDeleteSuccessfully() = runTest {
        coEvery { warehouseRepository.hasProducts(1) } returns false
        coEvery { warehouseRepository.delete(1) } returns true

        warehouseService.deleteWarehouseById(1)

        coVerify(exactly = 1) { warehouseRepository.delete(1) }
    }

    @Test
    fun deleteWarehouseById_shouldThrowWhenHasProducts() = runTest {
        coEvery { warehouseRepository.hasProducts(1) } returns true

        assertFailsWith<IllegalArgumentException> {
            warehouseService.deleteWarehouseById(1)
        }
        coVerify(exactly = 0) { warehouseRepository.delete(any()) }
    }

    @Test
    fun deleteWarehouseById_shouldThrowWhenNotFound() = runTest {
        coEvery { warehouseRepository.hasProducts(1) } returns false
        coEvery { warehouseRepository.delete(1) } returns false

        assertFailsWith<NoSuchElementException> {
            warehouseService.deleteWarehouseById(1)
        }
    }

    
    // getProductsByWarehouseId
    @Test
    fun getProductsByWarehouseId_shouldReturnProducts() = runTest {
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery { warehouseProductRepository.findByWarehouseWithDetails(1) } returns listOf(testProductDetail)

        val result = warehouseService.getProductsByWarehouseId(1)

        assertEquals(1, result.size)
        assertEquals(testProductDetail, result[0])
    }

    @Test
    fun getProductsByWarehouseId_shouldThrowWhenWarehouseNotFound() = runTest {
        coEvery { warehouseRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            warehouseService.getProductsByWarehouseId(99)
        }
        coVerify(exactly = 0) { warehouseProductRepository.findByWarehouse(any()) }
    }

    @Test
    fun getProductsByWarehouseId_shouldReturnEmptyWhenNoProducts() = runTest {
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery { warehouseProductRepository.findByWarehouseWithDetails(1) } returns emptyList()

        val result = warehouseService.getProductsByWarehouseId(1)

        assertTrue(result.isEmpty())
    }

    
    // getProductsGroupedByCategory
    @Test
    fun getProductsGroupedByCategory_shouldReturnGroupedProducts() = runTest {
        val grouped = mapOf("Electronics" to listOf(testProductDetail))
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery {
            warehouseProductRepository.findByWarehouseGroupedByCategory(1)
        } returns grouped

        val result = warehouseService.getProductsGroupedByCategory(1)

        assertEquals(1, result.size)
        assertTrue(result.containsKey("Electronics"))
        assertEquals(1, result["Electronics"]?.size)
    }

    @Test
    fun getProductsGroupedByCategory_shouldThrowWhenWarehouseNotFound() = runTest {
        coEvery { warehouseRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            warehouseService.getProductsGroupedByCategory(99)
        }
        coVerify(exactly = 0) {
            warehouseProductRepository.findByWarehouseGroupedByCategory(any())
        }
    }
    

    // getWarehouseStats
    @Test
    fun getWarehouseStats_shouldReturnStats() = runTest {
        val stats = WarehouseStats(totalQuantity = 20, totalPrice = 100000L, uniqueProducts = 1)
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery { warehouseProductRepository.getTotalStats(1) } returns stats

        val result = warehouseService.getWarehouseStats(1)

        assertEquals(20, result.totalQuantity)
        assertEquals(100000L, result.totalPrice)
        assertEquals(1, result.uniqueProducts)
    }

    @Test
    fun getWarehouseStats_shouldThrowWhenWarehouseNotFound() = runTest {
        coEvery { warehouseRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            warehouseService.getWarehouseStats(99)
        }
        coVerify(exactly = 0) { warehouseProductRepository.getTotalStats(any()) }
    }
}