package service

import api.dto.CreateSupplierRequest
import api.dto.UpdateSupplierRequest
import domain.model.Supplier
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SupplierServiceTest : BaseServiceTest() {

    private lateinit var service: SupplierService

    private fun initService() {
        service = SupplierService(supplierRepository)
    }

    @Test
    fun `getAllSuppliers should return list of suppliers`() = runTest {
        val expected = listOf(
            mockSupplier(1, "TechSupplier", "+79001234567", "tech@supplier.com"),
            mockSupplier(2, "FoodSupplier", "+79001234568", "food@supplier.com")
        )
        coEvery { supplierRepository.findAllByUserId(1) } returns expected
        initService()

        val result = service.getAllSuppliers(1)

        assertEquals(2, result.size)
        assertEquals("TechSupplier", result[0].name)
        coVerify { supplierRepository.findAllByUserId(1) }
    }

    @Test
    fun `getSupplierById should return supplier when exists`() = runTest {
        val expected = mockSupplier(1, "TechSupplier")
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns expected
        initService()

        val result = service.getSupplierById(1, 1)

        assertEquals("TechSupplier", result.name)
        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `getSupplierById should throw NoSuchElementException when not found`() = runTest {
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.getSupplierById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `createSupplier should create supplier with valid data`() = runTest {
        val dto = CreateSupplierRequest(
            name = "TechSupplier",
            phone = "+79001234567",
            email = "tech@supplier.com",
            address = "Moscow"
        )
        val expected = mockSupplier(1, "TechSupplier", "+79001234567", "tech@supplier.com")
        coEvery {
            supplierRepository.create("TechSupplier", "+79001234567", "tech@supplier.com", "Moscow", 1)
        } returns expected
        initService()

        val result = service.createSupplier(dto, 1)

        assertEquals("TechSupplier", result.name)
        coVerify {
            supplierRepository.create("TechSupplier", "+79001234567", "tech@supplier.com", "Moscow", 1)
        }
    }

    @Test
    fun `createSupplier should trim name and address`() = runTest {
        val dto = CreateSupplierRequest(
            name = "  TechSupplier  ",
            phone = "+79001234567",
            email = null,
            address = "  Moscow  "
        )
        val expected = mockSupplier(1, "TechSupplier", "+79001234567", null)
        coEvery {
            supplierRepository.create("TechSupplier", "+79001234567", null, "Moscow", 1)
        } returns expected
        initService()

        val result = service.createSupplier(dto, 1)

        assertEquals("TechSupplier", result.name)
        coVerify {
            supplierRepository.create("TechSupplier", "+79001234567", null, "Moscow", 1)
        }
    }

    @Test
    fun `createSupplier should throw IllegalArgumentException when name is blank`() = runTest {
        val dto = CreateSupplierRequest(name = "   ", phone = "+79001234567")
        initService()

        try {
            service.createSupplier(dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Name must not be blank", e.message)
        }
        coVerify(exactly = 0) { supplierRepository.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `createSupplier should throw IllegalArgumentException when phone format is invalid`() = runTest {
        val dto = CreateSupplierRequest(name = "TechSupplier", phone = "invalid")
        initService()

        try {
            service.createSupplier(dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid phone format", e.message)
        }
        coVerify(exactly = 0) { supplierRepository.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `createSupplier should accept phone starting with 8`() = runTest {
        val dto = CreateSupplierRequest(name = "TechSupplier", phone = "80001234567")
        val expected = mockSupplier(1, "TechSupplier", "80001234567", null)
        coEvery {
            supplierRepository.create("TechSupplier", "80001234567", null, null, 1)
        } returns expected
        initService()

        val result = service.createSupplier(dto, 1)

        assertEquals("80001234567", result.phone)
    }

    @Test
    fun `createSupplier should throw IllegalArgumentException when email format is invalid`() = runTest {
        val dto = CreateSupplierRequest(name = "TechSupplier", phone = "+79001234567", email = "invalid-email")
        initService()

        try {
            service.createSupplier(dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid email format", e.message)
        }
    }

    @Test
    fun `updateSupplierById should update supplier when exists`() = runTest {
        val dto = UpdateSupplierRequest(
            name = "Updated Supplier",
            phone = "+79001234568",
            email = "updated@supplier.com",
            address = "New Address"
        )
        val existing = mockSupplier(1, "TechSupplier")
        val expected = mockSupplier(1, "Updated Supplier", "+79001234568", "updated@supplier.com")
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns existing
        coEvery {
            supplierRepository.update(1, "Updated Supplier", "+79001234568", "updated@supplier.com", "New Address", 1)
        } returns expected
        initService()

        val result = service.updateSupplierById(1, dto, 1)

        assertEquals("Updated Supplier", result.name)
        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
        coVerify {
            supplierRepository.update(1, "Updated Supplier", "+79001234568", "updated@supplier.com", "New Address", 1)
        }
    }

    @Test
    fun `updateSupplierById should throw IllegalArgumentException when name is blank`() = runTest {
        val existing = mockSupplier(1, "TechSupplier")
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns existing
        initService()

        try {
            service.updateSupplierById(1, UpdateSupplierRequest(name = "   "), 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Name must not be blank", e.message)
        }
        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
        coVerify(exactly = 0) { supplierRepository.update(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateSupplierById should throw IllegalArgumentException when phone format is invalid`() = runTest {
        val existing = mockSupplier(1, "TechSupplier")
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns existing
        initService()

        try {
            service.updateSupplierById(1, UpdateSupplierRequest(phone = "invalid"), 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid phone format", e.message)
        }
    }

    @Test
    fun `updateSupplierById should throw IllegalArgumentException when email format is invalid`() = runTest {
        val existing = mockSupplier(1, "TechSupplier")
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns existing
        initService()

        try {
            service.updateSupplierById(1, UpdateSupplierRequest(email = "invalid-email"), 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid email format", e.message)
        }
    }

    @Test
    fun `updateSupplierById should throw NoSuchElementException when not found`() = runTest {
        val dto = UpdateSupplierRequest(name = "New Name")
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.updateSupplierById(1, dto, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `deleteSupplierById should delete supplier when exists and has no supplies`() = runTest {
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns mockSupplier(1)
        coEvery { supplierRepository.hasSupplies(1) } returns false
        coEvery { supplierRepository.delete(1, 1) } returns true
        initService()

        service.deleteSupplierById(1, 1)

        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
        coVerify { supplierRepository.hasSupplies(1) }
        coVerify { supplierRepository.delete(1, 1) }
    }

    @Test
    fun `deleteSupplierById should throw IllegalArgumentException when has supplies`() = runTest {
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns mockSupplier(1)
        coEvery { supplierRepository.hasSupplies(1) } returns true
        initService()

        try {
            service.deleteSupplierById(1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot delete supplier with existing supplies", e.message)
        }
        coVerify { supplierRepository.findByIdAndUserId(1, 1) }
        coVerify { supplierRepository.hasSupplies(1) }
        coVerify(exactly = 0) { supplierRepository.delete(any(), any()) }
    }

    @Test
    fun `deleteSupplierById should throw NoSuchElementException when not found`() = runTest {
        coEvery { supplierRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.deleteSupplierById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }
}
