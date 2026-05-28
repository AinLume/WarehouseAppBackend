package service

import domain.model.SupplyStatus
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SupplyServiceTest : BaseServiceTest() {

    private val testUserId = 1

    // getAllSupplies
    @Test
    fun getAllSupplies_shouldReturnAllSupplies() = runTest {
        coEvery { supplyRepository.findAllByUserId(testUserId, null, null) } returns testSupplies

        val result = supplyService.getAllSupplies(testUserId, null, null)

        assertEquals(testSupplies, result)
        coVerify(exactly = 1) { supplyRepository.findAllByUserId(testUserId, null, null) }
    }

    @Test
    fun getAllSupplies_shouldReturnEmptyList() = runTest {
        coEvery { supplyRepository.findAllByUserId(testUserId, null, null) } returns emptyList()

        val result = supplyService.getAllSupplies(testUserId, null, null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun getAllSupplies_shouldFilterByWarehouseAndStatus() = runTest {
        coEvery {
            supplyRepository.findAllByUserId(testUserId, 1, SupplyStatus.CREATED)
        } returns listOf(testSupplyEnriched)

        val result = supplyService.getAllSupplies(testUserId, 1, SupplyStatus.CREATED)

        assertEquals(1, result.size)
        assertEquals(SupplyStatus.CREATED, result[0].supply.status)
    }

    // getSupplyById
    @Test
    fun getSupplyById_shouldReturnSupply() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched

        val result = supplyService.getSupplyById(1L, testUserId)

        assertEquals(testSupplyEnriched, result)
        coVerify(exactly = 1) { supplyRepository.findByIdAndUserId(1L, testUserId) }
    }

    @Test
    fun getSupplyById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.getSupplyById(99L, testUserId)
        }
    }

    // getSupplyDetails
    @Test
    fun getSupplyDetails_shouldReturnDetails() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { supplyRepository.findProductsDetails(1L) } returns listOf(testSupplyProductDetail)

        val result = supplyService.getSupplyDetails(1L, testUserId)

        assertEquals(1, result.products.size)
        assertEquals(testSupplyProductDetail, result.products[0])
    }

    @Test
    fun getSupplyDetails_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.getSupplyDetails(99L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.findProductsDetails(any()) }
    }

    // createSupply
    @Test
    fun createSupply_shouldReturnCreatedSupply() = runTest {
        coEvery { supplierRepository.findById(1) } returns testSupplier
        coEvery { warehouseRepository.findByIdAndUserId(1, testUserId) } returns testWarehouse
        coEvery { supplyRepository.create(1, 1, testUserId) } returns testSupplyEnriched

        val result = supplyService.createSupply(createSupplyRequest, testUserId)

        assertEquals(SupplyStatus.CREATED, result.supply.status)
        assertEquals(1, result.supply.supplierId)
        coVerify(exactly = 1) { supplyRepository.create(1, 1, testUserId) }
    }

    @Test
    fun createSupply_shouldThrowWhenSupplierNotFound() = runTest {
        val request = createSupplyRequest.copy(supplierId = 99)
        coEvery { supplierRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.createSupply(request, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.create(any(), any(), any()) }
    }

    @Test
    fun createSupply_shouldThrowWhenWarehouseNotFound() = runTest {
        val request = createSupplyRequest.copy(warehouseId = 99)
        coEvery { supplierRepository.findById(1) } returns testSupplier
        coEvery { warehouseRepository.findByIdAndUserId(99, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.createSupply(request, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.create(any(), any(), any()) }
    }

    // updateSupplyStatus
    @Test
    fun updateSupplyStatus_shouldTransitionFromCreatedToPending() = runTest {
        val updated = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.PENDING))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { supplyRepository.updateStatus(1L, SupplyStatus.PENDING) } returns updated

        val result = supplyService.updateSupplyStatus(1L, SupplyStatus.PENDING, testUserId)

        assertEquals(SupplyStatus.PENDING, result.supply.status)
        coVerify(exactly = 1) { supplyRepository.updateStatus(1L, SupplyStatus.PENDING) }
    }

    @Test
    fun updateSupplyStatus_shouldTransitionFromCreatedToCancelled() = runTest {
        val updated = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.CANCELLED))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { supplyRepository.updateStatus(1L, SupplyStatus.CANCELLED) } returns updated

        val result = supplyService.updateSupplyStatus(1L, SupplyStatus.CANCELLED, testUserId)

        assertEquals(SupplyStatus.CANCELLED, result.supply.status)
    }

    @Test
    fun updateSupplyStatus_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.updateSupplyStatus(99L, SupplyStatus.PENDING, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun updateSupplyStatus_shouldThrowOnInvalidTransition() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched

        assertFailsWith<IllegalArgumentException> {
            supplyService.updateSupplyStatus(1L, SupplyStatus.COMPLETED, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun updateSupplyStatus_shouldThrowOnTransitionFromCancelled() = runTest {
        val cancelled = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.CANCELLED))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns cancelled

        assertFailsWith<IllegalArgumentException> {
            supplyService.updateSupplyStatus(1L, SupplyStatus.PENDING, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun updateSupplyStatus_shouldCompleteSupplyAndUpdateWarehouse() = runTest {
        val deliveredSupply = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.DELIVERED))
        val completed = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.COMPLETED))

        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns deliveredSupply
        coEvery { supplyRepository.findProducts(1L) } returns listOf(testSupplyProduct)
        coEvery { warehouseRepository.findBySupplyId(1L) } returns testWarehouse
        coEvery { warehouseProductRepository.upsertAll(1, any()) } returns Unit
        coEvery { supplyRepository.updateStatus(1L, SupplyStatus.COMPLETED) } returns completed

        val result = supplyService.updateSupplyStatus(1L, SupplyStatus.COMPLETED, testUserId)

        assertEquals(SupplyStatus.COMPLETED, result.supply.status)
        coVerify(exactly = 1) { warehouseProductRepository.upsertAll(1, any()) }
        coVerify(exactly = 1) { supplyRepository.findProducts(1L) }
    }

    @Test
    fun updateSupplyStatus_shouldThrowWhenWarehouseNotFoundOnComplete() = runTest {
        val deliveredSupply = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.DELIVERED))

        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns deliveredSupply
        coEvery { supplyRepository.findProducts(1L) } returns listOf(testSupplyProduct)
        coEvery { warehouseRepository.findBySupplyId(1L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.updateSupplyStatus(1L, SupplyStatus.COMPLETED, testUserId)
        }
        coVerify(exactly = 0) { warehouseProductRepository.upsertAll(any(), any()) }
    }

    // addProductToSupply
    @Test
    fun addProductToSupply_shouldReturnAddedProduct() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { productRepository.findByIdAndUserId(1L, testUserId) } returns testProduct
        coEvery { supplyRepository.addProduct(1L, 1L, 10, 5000L) } returns testSupplyProduct
        coEvery { supplyRepository.updateTotalPrice(1L) } returns Unit

        val result = supplyService.addProductToSupply(1L, addSupplyProductRequest, testUserId)

        assertEquals(testSupplyProduct, result)
        coVerify(exactly = 1) { supplyRepository.addProduct(1L, 1L, 10, 5000L) }
        coVerify(exactly = 1) { supplyRepository.updateTotalPrice(1L) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.addProductToSupply(99L, addSupplyProductRequest, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenSupplyNotInCreatedStatus() = runTest {
        val pendingSupply = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.PENDING))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns pendingSupply

        assertFailsWith<IllegalArgumentException> {
            supplyService.addProductToSupply(1L, addSupplyProductRequest, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenProductNotFound() = runTest {
        val request = addSupplyProductRequest.copy(productId = 99L)
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { productRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.addProductToSupply(1L, request, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenQuantityIsZero() = runTest {
        val request = addSupplyProductRequest.copy(quantity = 0)
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { productRepository.findByIdAndUserId(1L, testUserId) } returns testProduct

        assertFailsWith<IllegalArgumentException> {
            supplyService.addProductToSupply(1L, request, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenUnitPriceIsNegative() = runTest {
        val request = addSupplyProductRequest.copy(unitPrice = -1L)
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { productRepository.findByIdAndUserId(1L, testUserId) } returns testProduct

        assertFailsWith<IllegalArgumentException> {
            supplyService.addProductToSupply(1L, request, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    // removeProduct
    @Test
    fun removeProduct_shouldRemoveSuccessfully() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { supplyRepository.removeProduct(1L, 1L) } returns true
        coEvery { supplyRepository.updateTotalPrice(1L) } returns Unit

        supplyService.removeProduct(1L, 1L, testUserId)

        coVerify(exactly = 1) { supplyRepository.removeProduct(1L, 1L) }
        coVerify(exactly = 1) { supplyRepository.updateTotalPrice(1L) }
    }

    @Test
    fun removeProduct_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.removeProduct(99L, 1L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.removeProduct(any(), any()) }
    }

    @Test
    fun removeProduct_shouldThrowWhenSupplyNotInCreatedStatus() = runTest {
        val pendingSupply = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.PENDING))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns pendingSupply

        assertFailsWith<IllegalArgumentException> {
            supplyService.removeProduct(1L, 1L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.removeProduct(any(), any()) }
    }

    @Test
    fun removeProduct_shouldThrowWhenProductNotInSupply() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { supplyRepository.removeProduct(1L, 99L) } returns false

        assertFailsWith<NoSuchElementException> {
            supplyService.removeProduct(1L, 99L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.updateTotalPrice(any()) }
    }

    // deleteSupplyById
    @Test
    fun deleteSupplyById_shouldDeleteCreatedSupply() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns testSupplyEnriched
        coEvery { supplyRepository.delete(1L) } returns true

        supplyService.deleteSupplyById(1L, testUserId)

        coVerify(exactly = 1) { supplyRepository.delete(1L) }
    }

    @Test
    fun deleteSupplyById_shouldDeleteCancelledSupply() = runTest {
        val cancelled = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.CANCELLED))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns cancelled
        coEvery { supplyRepository.delete(1L) } returns true

        supplyService.deleteSupplyById(1L, testUserId)

        coVerify(exactly = 1) { supplyRepository.delete(1L) }
    }

    @Test
    fun deleteSupplyById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplyRepository.findByIdAndUserId(99L, testUserId) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.deleteSupplyById(99L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }

    @Test
    fun deleteSupplyById_shouldThrowWhenStatusIsShipped() = runTest {
        val shipped = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.SHIPPED))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns shipped

        assertFailsWith<IllegalArgumentException> {
            supplyService.deleteSupplyById(1L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }

    @Test
    fun deleteSupplyById_shouldThrowWhenStatusIsCompleted() = runTest {
        val completed = testSupplyEnriched.copy(supply = testSupplyEnriched.supply.copy(status = SupplyStatus.COMPLETED))
        coEvery { supplyRepository.findByIdAndUserId(1L, testUserId) } returns completed

        assertFailsWith<IllegalArgumentException> {
            supplyService.deleteSupplyById(1L, testUserId)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }

    // ============== Internal methods tests ==============
    @Test
    fun getSupplyByIdInternal_shouldReturnSupply() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupplyEnriched

        val result = supplyService.getSupplyByIdInternal(1L)

        assertEquals(testSupplyEnriched, result)
    }

    @Test
    fun getSupplyByIdInternal_shouldThrowWhenNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.getSupplyByIdInternal(99L)
        }
    }

    @Test
    fun hasAccess_shouldReturnTrue() = runTest {
        coEvery { supplyRepository.hasAccess(1L, testUserId) } returns true

        val result = supplyService.hasAccess(1L, testUserId)

        assertTrue(result)
    }

    @Test
    fun hasAccess_shouldReturnFalse() = runTest {
        coEvery { supplyRepository.hasAccess(1L, testUserId) } returns false

        val result = supplyService.hasAccess(1L, testUserId)

        assertFalse(result)
    }
}
