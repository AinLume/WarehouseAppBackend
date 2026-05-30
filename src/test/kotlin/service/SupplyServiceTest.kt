package service

import api.dto.AddSupplyProductRequest
import api.dto.CreateSupplyRequest
import domain.model.Supply
import domain.model.SupplyStatus
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SupplyServiceTest : BaseServiceTest() {

    private lateinit var service: SupplyService

    private fun initService() {
        service = SupplyService(
            supplyRepository,
            SupplierService(supplierRepository),
            WarehouseService(warehouseRepository, warehouseProductRepository),
            ProductService(productRepository),
            warehouseProductRepository
        )
    }

    @Test
    fun `getAllSupplies should return list of supplies`() = runTest {
        val expected = listOf(
            mockSupplyEnriched(mockSupply(1), 1, "Main Warehouse", "TechSupplier"),
            mockSupplyEnriched(mockSupply(2), 2, "Secondary Warehouse", "FoodSupplier")
        )
        coEvery { supplyRepository.findAllByUserId(1, null, null) } returns expected
        initService()

        val result = service.getAllSupplies(1, null, null)

        assertEquals(2, result.size)
        coVerify { supplyRepository.findAllByUserId(1, null, null) }
    }

    @Test
    fun `getAllSupplies should filter by warehouseId and status`() = runTest {
        val expected = listOf(mockSupplyEnriched(mockSupply(1), 1, "Main Warehouse", "TechSupplier"))
        coEvery { supplyRepository.findAllByUserId(1, 1, SupplyStatus.PENDING) } returns expected
        initService()

        val result = service.getAllSupplies(1, 1, SupplyStatus.PENDING)

        assertEquals(1, result.size)
        coVerify { supplyRepository.findAllByUserId(1, 1, SupplyStatus.PENDING) }
    }

    @Test
    fun `getSupplyById should return supply when exists and belongs to user`() = runTest {
        val expected = mockSupplyEnriched(mockSupply(1), 1, "Main Warehouse", "TechSupplier")
        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns expected
        initService()

        val result = service.getSupplyById(1, 1)

        assertEquals(1L, result.supply.supplyId)
        coVerify { supplyRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `getSupplyById should throw NoSuchElementException when not found`() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.getSupplyById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `createSupply should create supply with valid supplier and warehouse`() = runTest {
        val dto = CreateSupplyRequest(supplierId = 1, warehouseId = 1)
        val supplier = mockSupplier(1, "TechSupplier")
        val warehouse = mockWarehouse(1, "Main Warehouse")
        val expected = mockSupplyEnriched(mockSupply(1), 1, "Main Warehouse", "TechSupplier")

        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns supplier
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns warehouse
        coEvery { supplyRepository.create(1, 1, 1) } returns expected
        initService()

        val result = service.createSupply(dto, 1)

        assertEquals(1L, result.supply.supplyId)
        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
        coVerify { warehouseRepository.findByIdAndUserId(1, 1) }
        coVerify { supplyRepository.create(1, 1, 1) }
    }

    @Test
    fun `createSupply should throw NoSuchElementException when supplier not found`() = runTest {
        val dto = CreateSupplyRequest(supplierId = 1, warehouseId = 1)
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.createSupply(dto, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `createSupply should throw NoSuchElementException when warehouse not found`() = runTest {
        val dto = CreateSupplyRequest(supplierId = 1, warehouseId = 1)
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns mockSupplier(1)
        coEvery { warehouseRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.createSupply(dto, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `updateSupplyStatus should update status for valid transition`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.PENDING)
        val enriched = mockSupplyEnriched(supply, 1)
        val updated = mockSupplyEnriched(mockSupply(1, status = SupplyStatus.CONFIRMED), 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { supplyRepository.updateStatus(1, SupplyStatus.CONFIRMED) } returns updated
        initService()

        val result = service.updateSupplyStatus(1, SupplyStatus.CONFIRMED, 1)

        assertEquals(SupplyStatus.CONFIRMED, result.supply.status)
        coVerify { supplyRepository.findByIdAndUserId(1, 1) }
        coVerify { supplyRepository.updateStatus(1, SupplyStatus.CONFIRMED) }
    }

    @Test
    fun `updateSupplyStatus should throw IllegalArgumentException for invalid transition`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)
        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        initService()

        try {
            service.updateSupplyStatus(1, SupplyStatus.COMPLETED, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot transition from CREATED to COMPLETED", e.message)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun `addProductToSupply should add product when supply is in CREATED status`() = runTest {
        val dto = AddSupplyProductRequest(productId = 1, quantity = 10, unitPrice = 1000L)
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)
        val product = mockProduct(1)
        val expected = mockSupplyProduct(1, 1, 1, 10, 1000L)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { productRepository.findByIdAndUserId(1, 1) } returns product
        coEvery { supplyRepository.addProduct(1, 1, 10, 1000L) } returns expected
        coEvery { supplyRepository.updateTotalPrice(1) } returns Unit
        initService()

        val result = service.addProductToSupply(1, dto, 1)

        assertEquals(10, result.quantity)
        coVerify { supplyRepository.findByIdAndUserId(1, 1) }
        coVerify { productRepository.findByIdAndUserId(1, 1) }
        coVerify { supplyRepository.addProduct(1, 1, 10, 1000L) }
        coVerify { supplyRepository.updateTotalPrice(1) }
    }

    @Test
    fun `addProductToSupply should throw IllegalArgumentException when supply not in CREATED status`() = runTest {
        val dto = AddSupplyProductRequest(productId = 1, quantity = 10, unitPrice = 1000L)
        val supply = mockSupply(1, status = SupplyStatus.CONFIRMED)
        val enriched = mockSupplyEnriched(supply, 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        initService()

        try {
            service.addProductToSupply(1, dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Can only add products to supply with status CREATED", e.message)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun `addProductToSupply should throw IllegalArgumentException when quantity is not positive`() = runTest {
        val dto = AddSupplyProductRequest(productId = 1, quantity = 0, unitPrice = 1000L)
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)
        val product = mockProduct(1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { productRepository.findByIdAndUserId(1, 1) } returns product
        initService()

        try {
            service.addProductToSupply(1, dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Quantity must be positive", e.message)
        }
    }

    @Test
    fun `addProductToSupply should throw IllegalArgumentException when unitPrice is not positive`() = runTest {
        val dto = AddSupplyProductRequest(productId = 1, quantity = 10, unitPrice = 0)
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)
        val product = mockProduct(1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { productRepository.findByIdAndUserId(1, 1) } returns product
        initService()

        try {
            service.addProductToSupply(1, dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Unit price must be positive", e.message)
        }
    }

    @Test
    fun `removeProduct should remove product when supply is in CREATED status`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { supplyRepository.removeProduct(1, 1) } returns true
        coEvery { supplyRepository.updateTotalPrice(1) } returns Unit
        initService()

        service.removeProduct(1, 1, 1)

        coVerify { supplyRepository.findByIdAndUserId(1, 1) }
        coVerify { supplyRepository.removeProduct(1, 1) }
        coVerify { supplyRepository.updateTotalPrice(1) }
    }

    @Test
    fun `removeProduct should throw IllegalArgumentException when supply not in CREATED status`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.CONFIRMED)
        val enriched = mockSupplyEnriched(supply, 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        initService()

        try {
            service.removeProduct(1, 1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Can only remove products from supply with status CREATED", e.message)
        }
        coVerify(exactly = 0) { supplyRepository.removeProduct(any(), any()) }
    }

    @Test
    fun `removeProduct should throw NoSuchElementException when product not found in supply`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { supplyRepository.removeProduct(1, 1) } returns false
        initService()

        try {
            service.removeProduct(1, 1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `deleteSupplyById should delete supply when in CREATED status`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.CREATED)
        val enriched = mockSupplyEnriched(supply, 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        coEvery { supplyRepository.delete(1) } returns true
        initService()

        service.deleteSupplyById(1, 1)

        coVerify { supplyRepository.findByIdAndUserId(1, 1) }
        coVerify { supplyRepository.delete(1) }
    }

    @Test
    fun `deleteSupplyById should throw IllegalArgumentException when in CONFIRMED status`() = runTest {
        val supply = mockSupply(1, status = SupplyStatus.CONFIRMED)
        val enriched = mockSupplyEnriched(supply, 1)

        coEvery { supplyRepository.findByIdAndUserId(1, 1) } returns enriched
        initService()

        try {
            service.deleteSupplyById(1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Can only delete supply with status CREATED or CANCELLED", e.message)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }

    @Test
    fun `getSupplyByIdInternal should return supply when exists`() = runTest {
        val expected = mockSupplyEnriched(mockSupply(1), 1)
        coEvery { supplyRepository.findById(1) } returns expected
        initService()

        val result = service.getSupplyByIdInternal(1)

        assertEquals(1L, result.supply.supplyId)
        coVerify { supplyRepository.findById(1) }
    }

    @Test
    fun `getSupplyByIdInternal should throw NoSuchElementException when not found`() = runTest {
        coEvery { supplyRepository.findById(1) } returns null
        initService()

        try {
            service.getSupplyByIdInternal(1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `hasAccess should return true when user has access`() = runTest {
        coEvery { supplyRepository.hasAccess(1, 1) } returns true
        initService()

        val result = service.hasAccess(1, 1)

        assertEquals(true, result)
    }

    @Test
    fun `hasAccess should return false when user has no access`() = runTest {
        coEvery { supplyRepository.hasAccess(1, 2) } returns false
        initService()

        val result = service.hasAccess(1, 2)

        assertEquals(false, result)
    }
}
