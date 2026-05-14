package service

import domain.model.SupplyStatus
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SupplyServiceTest : BaseServiceTest() {

    // getAllSupplies 
    @Test
    fun getAllSupplies_shouldReturnAllSupplies() = runTest {
        coEvery { supplyRepository.findAll(null, null) } returns testSupplies

        val result = supplyService.getAllSupplies(null, null)

        assertEquals(testSupplies, result)
        coVerify(exactly = 1) { supplyRepository.findAll(null, null) }
    }

    @Test
    fun getAllSupplies_shouldReturnEmptyList() = runTest {
        coEvery { supplyRepository.findAll(null, null) } returns emptyList()

        val result = supplyService.getAllSupplies(null, null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun getAllSupplies_shouldFilterByWarehouseAndStatus() = runTest {
        coEvery {
            supplyRepository.findAll(1, SupplyStatus.CREATED)
        } returns listOf(testSupply)

        val result = supplyService.getAllSupplies(1, SupplyStatus.CREATED)

        assertEquals(1, result.size)
        assertEquals(SupplyStatus.CREATED, result[0].status)
    }

    // getSupplyById
    @Test
    fun getSupplyById_shouldReturnSupply() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply

        val result = supplyService.getSupplyById(1L)

        assertEquals(testSupply, result)
    }

    @Test
    fun getSupplyById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.getSupplyById(99L)
        }
    }

    // getProductsBySupplyId
    @Test
    fun getProductsBySupplyId_shouldReturnProducts() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.findProductsDetails(1L) } returns listOf(testSupplyProductDetail)

        val result = supplyService.getProductsBySupplyId(1L)

        assertEquals(1, result.size)
        assertEquals(testSupplyProductDetail, result[0])
    }

    @Test
    fun getProductsBySupplyId_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.getProductsBySupplyId(99L)
        }
        coVerify(exactly = 0) { supplyRepository.findProducts(any()) }
    }

    @Test
    fun getProductsBySupplyId_shouldReturnEmptyWhenNoProducts() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.findProductsDetails(1L) } returns emptyList()

        val result = supplyService.getProductsBySupplyId(1L)

        assertTrue(result.isEmpty())
    }

    // createSupply
    @Test
    fun createSupply_shouldReturnCreatedSupply() = runTest {
        coEvery { supplierRepository.findById(1) } returns testSupplier
        coEvery { warehouseRepository.findById(1) } returns testWarehouse
        coEvery { supplyRepository.create(1, 1) } returns testSupply

        val result = supplyService.createSupply(createSupplyRequest)

        assertEquals(SupplyStatus.CREATED, result.status)
        assertEquals(1, result.supplierId)
        coVerify(exactly = 1) { supplyRepository.create(1, 1) }
    }

    @Test
    fun createSupply_shouldThrowWhenSupplierNotFound() = runTest {
        val request = createSupplyRequest.copy(supplierId = 99)
        coEvery { supplierRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.createSupply(request)
        }
        coVerify(exactly = 0) { supplyRepository.create(any(), any()) }
    }

    @Test
    fun createSupply_shouldThrowWhenWarehouseNotFound() = runTest {
        val request = createSupplyRequest.copy(warehouseId = 99)
        coEvery { supplierRepository.findById(1) } returns testSupplier
        coEvery { warehouseRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.createSupply(request)
        }
        coVerify(exactly = 0) { supplyRepository.create(any(), any()) }
    }

    // updateSupplyStatus
    @Test
    fun updateSupplyStatus_shouldTransitionFromCreatedToPending() = runTest {
        val updated = testSupply.copy(status = SupplyStatus.PENDING)
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.updateStatus(1L, SupplyStatus.PENDING) } returns updated

        val result = supplyService.updateSupplyStatus(1L, SupplyStatus.PENDING)

        assertEquals(SupplyStatus.PENDING, result.status)
        coVerify(exactly = 1) { supplyRepository.updateStatus(1L, SupplyStatus.PENDING) }
    }

    @Test
    fun updateSupplyStatus_shouldTransitionFromCreatedToCancelled() = runTest {
        val updated = testSupply.copy(status = SupplyStatus.CANCELLED)
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.updateStatus(1L, SupplyStatus.CANCELLED) } returns updated

        val result = supplyService.updateSupplyStatus(1L, SupplyStatus.CANCELLED)

        assertEquals(SupplyStatus.CANCELLED, result.status)
    }

    @Test
    fun updateSupplyStatus_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.updateSupplyStatus(99L, SupplyStatus.PENDING)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun updateSupplyStatus_shouldThrowOnInvalidTransition() = runTest {
        // CREATED → COMPLETED недопустимо
        coEvery { supplyRepository.findById(1L) } returns testSupply

        assertFailsWith<IllegalArgumentException> {
            supplyService.updateSupplyStatus(1L, SupplyStatus.COMPLETED)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun updateSupplyStatus_shouldThrowOnTransitionFromCancelled() = runTest {
        val cancelled = testSupply.copy(status = SupplyStatus.CANCELLED)
        coEvery { supplyRepository.findById(1L) } returns cancelled

        assertFailsWith<IllegalArgumentException> {
            supplyService.updateSupplyStatus(1L, SupplyStatus.PENDING)
        }
        coVerify(exactly = 0) { supplyRepository.updateStatus(any(), any()) }
    }

    @Test
    fun updateSupplyStatus_shouldCompleteSupplyAndUpdateWarehouse() = runTest {
        val deliveredSupply = testSupply.copy(status = SupplyStatus.DELIVERED)
        val completed = testSupply.copy(status = SupplyStatus.COMPLETED)

        coEvery { supplyRepository.findById(1L) } returns deliveredSupply
        coEvery { supplyRepository.findProducts(1L) } returns listOf(testSupplyProduct)
        coEvery { warehouseRepository.findBySupplyId(1L) } returns testWarehouse
        coEvery { warehouseProductRepository.upsertAll(1, any()) } returns Unit
        coEvery { supplyRepository.updateStatus(1L, SupplyStatus.COMPLETED) } returns completed

        val result = supplyService.updateSupplyStatus(1L, SupplyStatus.COMPLETED)

        assertEquals(SupplyStatus.COMPLETED, result.status)
        coVerify(exactly = 1) { warehouseProductRepository.upsertAll(1, any()) }
        coVerify(exactly = 1) { supplyRepository.findProducts(1L) }
    }

    @Test
    fun updateSupplyStatus_shouldThrowWhenWarehouseNotFoundOnComplete() = runTest {
        val deliveredSupply = testSupply.copy(status = SupplyStatus.DELIVERED)

        coEvery { supplyRepository.findById(1L) } returns deliveredSupply
        coEvery { supplyRepository.findProducts(1L) } returns listOf(testSupplyProduct)
        coEvery { warehouseRepository.findBySupplyId(1L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.updateSupplyStatus(1L, SupplyStatus.COMPLETED)
        }
        coVerify(exactly = 0) { warehouseProductRepository.upsertAll(any(), any()) }
    }

    // addProductToSupply
    @Test
    fun addProductToSupply_shouldReturnAddedProduct() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { productRepository.findById(1L) } returns testProduct
        coEvery { supplyRepository.addProduct(1L, 1L, 10, 5000L) } returns testSupplyProduct
        coEvery { supplyRepository.updateTotalPrice(1L) } returns Unit

        val result = supplyService.addProductToSupply(1L, addSupplyProductRequest)

        assertEquals(testSupplyProduct, result)
        coVerify(exactly = 1) { supplyRepository.addProduct(1L, 1L, 10, 5000L) }
        coVerify(exactly = 1) { supplyRepository.updateTotalPrice(1L) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.addProductToSupply(99L, addSupplyProductRequest)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenSupplyNotInCreatedStatus() = runTest {
        val pendingSupply = testSupply.copy(status = SupplyStatus.PENDING)
        coEvery { supplyRepository.findById(1L) } returns pendingSupply

        assertFailsWith<IllegalArgumentException> {
            supplyService.addProductToSupply(1L, addSupplyProductRequest)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenProductNotFound() = runTest {
        val request = addSupplyProductRequest.copy(productId = 99L)
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { productRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.addProductToSupply(1L, request)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenQuantityIsZero() = runTest {
        val request = addSupplyProductRequest.copy(quantity = 0)
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { productRepository.findById(1L) } returns testProduct

        assertFailsWith<IllegalArgumentException> {
            supplyService.addProductToSupply(1L, request)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    @Test
    fun addProductToSupply_shouldThrowWhenUnitPriceIsNegative() = runTest {
        val request = addSupplyProductRequest.copy(unitPrice = -1L)
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { productRepository.findById(1L) } returns testProduct

        assertFailsWith<IllegalArgumentException> {
            supplyService.addProductToSupply(1L, request)
        }
        coVerify(exactly = 0) { supplyRepository.addProduct(any(), any(), any(), any()) }
    }

    // removeProduct
    @Test
    fun removeProduct_shouldRemoveSuccessfully() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.removeProduct(1L, 1L) } returns true
        coEvery { supplyRepository.updateTotalPrice(1L) } returns Unit

        supplyService.removeProduct(1L, 1L)

        coVerify(exactly = 1) { supplyRepository.removeProduct(1L, 1L) }
        coVerify(exactly = 1) { supplyRepository.updateTotalPrice(1L) }
    }

    @Test
    fun removeProduct_shouldThrowWhenSupplyNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.removeProduct(99L, 1L)
        }
        coVerify(exactly = 0) { supplyRepository.removeProduct(any(), any()) }
    }

    @Test
    fun removeProduct_shouldThrowWhenSupplyNotInCreatedStatus() = runTest {
        val pendingSupply = testSupply.copy(status = SupplyStatus.PENDING)
        coEvery { supplyRepository.findById(1L) } returns pendingSupply

        assertFailsWith<IllegalArgumentException> {
            supplyService.removeProduct(1L, 1L)
        }
        coVerify(exactly = 0) { supplyRepository.removeProduct(any(), any()) }
    }

    @Test
    fun removeProduct_shouldThrowWhenProductNotInSupply() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.removeProduct(1L, 99L) } returns false

        assertFailsWith<NoSuchElementException> {
            supplyService.removeProduct(1L, 99L)
        }
        coVerify(exactly = 0) { supplyRepository.updateTotalPrice(any()) }
    }

    // deleteSupplyById
    @Test
    fun deleteSupplyById_shouldDeleteCreatedSupply() = runTest {
        coEvery { supplyRepository.findById(1L) } returns testSupply
        coEvery { supplyRepository.delete(1L) } returns true

        supplyService.deleteSupplyById(1L)

        coVerify(exactly = 1) { supplyRepository.delete(1L) }
    }

    @Test
    fun deleteSupplyById_shouldDeleteCancelledSupply() = runTest {
        val cancelled = testSupply.copy(status = SupplyStatus.CANCELLED)
        coEvery { supplyRepository.findById(1L) } returns cancelled
        coEvery { supplyRepository.delete(1L) } returns true

        supplyService.deleteSupplyById(1L)

        coVerify(exactly = 1) { supplyRepository.delete(1L) }
    }

    @Test
    fun deleteSupplyById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplyRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            supplyService.deleteSupplyById(99L)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }

    @Test
    fun deleteSupplyById_shouldThrowWhenStatusIsShipped() = runTest {
        val shipped = testSupply.copy(status = SupplyStatus.SHIPPED)
        coEvery { supplyRepository.findById(1L) } returns shipped

        assertFailsWith<IllegalArgumentException> {
            supplyService.deleteSupplyById(1L)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }

    @Test
    fun deleteSupplyById_shouldThrowWhenStatusIsCompleted() = runTest {
        val completed = testSupply.copy(status = SupplyStatus.COMPLETED)
        coEvery { supplyRepository.findById(1L) } returns completed

        assertFailsWith<IllegalArgumentException> {
            supplyService.deleteSupplyById(1L)
        }
        coVerify(exactly = 0) { supplyRepository.delete(any()) }
    }
}
