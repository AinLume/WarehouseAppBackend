package service

import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SupplierServiceTest : BaseServiceTest() {

    // getAllSuppliers
    @Test
    fun getAllSuppliers_shouldReturnAllSuppliers() = runTest {
        coEvery { supplierRepository.findAll() } returns testSuppliers

        val result = supplierService.getAllSuppliers()

        assertEquals(testSuppliers, result)
        coVerify(exactly = 1) { supplierRepository.findAll() }
    }

    @Test
    fun getAllSuppliers_shouldReturnEmptyList() = runTest {
        coEvery { supplierRepository.findAll() } returns emptyList()

        val result = supplierService.getAllSuppliers()

        assertTrue(result.isEmpty())
    }

    // getSupplierById 

    @Test
    fun getSupplierById_shouldReturnSupplier() = runTest {
        coEvery { supplierRepository.findById(1) } returns testSupplier

        val result = supplierService.getSupplierById(1)

        assertEquals(testSupplier, result)
    }

    @Test
    fun getSupplierById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplierRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            supplierService.getSupplierById(99)
        }
    }

    // createSupplier
    @Test
    fun createSupplier_shouldReturnCreatedSupplier() = runTest {
        coEvery {
            supplierRepository.create("ООО Поставщик", "+79991234567", "supplier@example.com", any())
        } returns testSupplier

        val result = supplierService.createSupplier(createSupplierRequest)

        assertEquals(testSupplier, result)
        coVerify(exactly = 1) {
            supplierRepository.create("ООО Поставщик", "+79991234567", "supplier@example.com", any())
        }
    }

    @Test
    fun createSupplier_shouldThrowWhenNameIsBlank() = runTest {
        val request = createSupplierRequest.copy(name = "   ")

        assertFailsWith<IllegalArgumentException> {
            supplierService.createSupplier(request)
        }
        coVerify(exactly = 0) { supplierRepository.create(any(), any(), any(), any()) }
    }

    @Test
    fun createSupplier_shouldThrowWhenPhoneIsInvalid() = runTest {
        val request = createSupplierRequest.copy(phone = "12345")

        assertFailsWith<IllegalArgumentException> {
            supplierService.createSupplier(request)
        }
        coVerify(exactly = 0) { supplierRepository.create(any(), any(), any(), any()) }
    }

    @Test
    fun createSupplier_shouldThrowWhenEmailIsInvalid() = runTest {
        val request = createSupplierRequest.copy(email = "not-an-email")

        assertFailsWith<IllegalArgumentException> {
            supplierService.createSupplier(request)
        }
        coVerify(exactly = 0) { supplierRepository.create(any(), any(), any(), any()) }
    }

    @Test
    fun createSupplier_shouldAcceptPhoneStartingWith8() = runTest {
        val request = createSupplierRequest.copy(phone = "89991234567")
        coEvery {
            supplierRepository.create("ООО Поставщик", "89991234567", any(), any())
        } returns testSupplier.copy(phone = "89991234567")

        val result = supplierService.createSupplier(request)

        assertEquals("89991234567", result.phone)
    }

    @Test
    fun createSupplier_shouldCreateWithNullEmailAndAddress() = runTest {
        val request = createSupplierRequest.copy(email = null, address = null)
        val supplierNoContacts = testSupplier.copy(email = null, address = null)
        coEvery {
            supplierRepository.create("ООО Поставщик", "+79991234567", null, null)
        } returns supplierNoContacts

        val result = supplierService.createSupplier(request)

        assertNull(result.email)
        assertNull(result.address)
    }

    @Test
    fun createSupplier_shouldTrimNameBeforeSaving() = runTest {
        val request = createSupplierRequest.copy(name = "  ООО Поставщик  ")
        coEvery {
            supplierRepository.create("ООО Поставщик", any(), any(), any())
        } returns testSupplier

        supplierService.createSupplier(request)

        coVerify { supplierRepository.create("ООО Поставщик", any(), any(), any()) }
    }

    // updateSupplierById
    @Test
    fun updateSupplierById_shouldReturnUpdatedSupplier() = runTest {
        val updated = testSupplier.copy(name = "Новый поставщик")
        coEvery { supplierRepository.findById(1) } returns testSupplier
        coEvery {
            supplierRepository.update(1, "Новый поставщик", any(), any(), any())
        } returns updated

        val result = supplierService.updateSupplierById(1, updateSupplierRequest)

        assertEquals("Новый поставщик", result.name)
        coVerify(exactly = 1) {
            supplierRepository.update(1, "Новый поставщик", any(), any(), any())
        }
    }

    @Test
    fun updateSupplierById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplierRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            supplierService.updateSupplierById(99, updateSupplierRequest)
        }
        coVerify(exactly = 0) { supplierRepository.update(any(), any(), any(), any(), any()) }
    }

    @Test
    fun updateSupplierById_shouldThrowWhenNameIsBlank() = runTest {
        val request = updateSupplierRequest.copy(name = "   ")
        coEvery { supplierRepository.findById(1) } returns testSupplier

        assertFailsWith<IllegalArgumentException> {
            supplierService.updateSupplierById(1, request)
        }
        coVerify(exactly = 0) { supplierRepository.update(any(), any(), any(), any(), any()) }
    }

    @Test
    fun updateSupplierById_shouldThrowWhenPhoneIsInvalid() = runTest {
        val request = updateSupplierRequest.copy(phone = "12345")
        coEvery { supplierRepository.findById(1) } returns testSupplier

        assertFailsWith<IllegalArgumentException> {
            supplierService.updateSupplierById(1, request)
        }
        coVerify(exactly = 0) { supplierRepository.update(any(), any(), any(), any(), any()) }
    }

    @Test
    fun updateSupplierById_shouldThrowWhenEmailIsInvalid() = runTest {
        val request = updateSupplierRequest.copy(email = "bad-email")
        coEvery { supplierRepository.findById(1) } returns testSupplier

        assertFailsWith<IllegalArgumentException> {
            supplierService.updateSupplierById(1, request)
        }
        coVerify(exactly = 0) { supplierRepository.update(any(), any(), any(), any(), any()) }
    }

    @Test
    fun updateSupplierById_shouldUpdateOnlyProvidedFields() = runTest {
        val request = updateSupplierRequest.copy(name = null, phone = null)
        val updated = testSupplier.copy(email = "new@example.com")
        coEvery { supplierRepository.findById(1) } returns testSupplier
        coEvery {
            supplierRepository.update(1, null, null, "new@example.com", any())
        } returns updated

        val result = supplierService.updateSupplierById(1, request.copy(email = "new@example.com"))

        assertEquals("new@example.com", result.email)
        assertEquals(testSupplier.name, result.name)
    }

    // deleteSupplierById
    @Test
    fun deleteSupplierById_shouldDeleteSuccessfully() = runTest {
        coEvery { supplierRepository.hasSupplies(1) } returns false
        coEvery { supplierRepository.delete(1) } returns true

        supplierService.deleteSupplierById(1)

        coVerify(exactly = 1) { supplierRepository.delete(1) }
    }

    @Test
    fun deleteSupplierById_shouldThrowWhenHasSupplies() = runTest {
        coEvery { supplierRepository.hasSupplies(1) } returns true

        assertFailsWith<IllegalArgumentException> {
            supplierService.deleteSupplierById(1)
        }
        coVerify(exactly = 0) { supplierRepository.delete(any()) }
    }

    @Test
    fun deleteSupplierById_shouldThrowWhenNotFound() = runTest {
        coEvery { supplierRepository.hasSupplies(1) } returns false
        coEvery { supplierRepository.delete(1) } returns false

        assertFailsWith<NoSuchElementException> {
            supplierService.deleteSupplierById(1)
        }
    }
}