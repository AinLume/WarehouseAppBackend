package service

import api.dto.CreateProductRequest
import api.dto.UpdateProductRequest
import domain.model.Product
import domain.model.ProductEx
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProductServiceTest : BaseServiceTest() {

    private lateinit var service: ProductService

    private fun initService() {
        service = ProductService(productRepository)
    }

    @Test
    fun `getAllProducts should return list of products`() = runTest {
        val expected = listOf(
            mockProductEx(1, 1, "Electronics", "Laptop"),
            mockProductEx(2, 1, "Electronics", "Mouse")
        )
        coEvery { productRepository.findAllByUserId(1, null, null) } returns expected
        initService()

        val result = service.getAllProducts(null, null, 1)

        assertEquals(2, result.size)
        assertEquals("Laptop", result[0].title)
        coVerify { productRepository.findAllByUserId(1, null, null) }
    }

    @Test
    fun `getAllProducts should filter by categoryId`() = runTest {
        val expected = listOf(mockProductEx(1, 5, "Furniture", "Chair"))
        coEvery { productRepository.findAllByUserId(1, 5, null) } returns expected
        initService()

        val result = service.getAllProducts(5, null, 1)

        assertEquals(1, result.size)
        assertEquals("Chair", result[0].title)
        coVerify { productRepository.findAllByUserId(1, 5, null) }
    }

    @Test
    fun `getProductById should return product when exists and belongs to user`() = runTest {
        val expected = mockProduct(1, 1, "Laptop")
        coEvery { productRepository.findByIdAndUserId(1, 1) } returns expected
        initService()

        val result = service.getProductById(1, 1)

        assertEquals("Laptop", result.title)
        coVerify { productRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `getProductById should throw NoSuchElementException when not found`() = runTest {
        coEvery { productRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.getProductById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `createProduct should create product with valid data`() = runTest {
        val dto = CreateProductRequest(
            categoryId = 1,
            title = "Laptop",
            description = "Gaming laptop",
            width = 30,
            length = 20,
            height = 5
        )
        val expected = mockProduct(1, 1, "Laptop")
        coEvery {
            productRepository.create(
                categoryId = 1,
                title = "Laptop",
                description = "Gaming laptop",
                width = 30,
                length = 20,
                height = 5,
                userId = 1
            )
        } returns expected
        initService()

        val result = service.createProduct(dto, 1)

        assertEquals("Laptop", result.title)
        coVerify {
            productRepository.create(
                categoryId = 1,
                title = "Laptop",
                description = "Gaming laptop",
                width = 30,
                length = 20,
                height = 5,
                userId = 1
            )
        }
    }

    @Test
    fun `createProduct should trim title and description`() = runTest {
        val dto = CreateProductRequest(
            categoryId = 1,
            title = "  Laptop  ",
            description = "  Gaming laptop  ",
            width = 30,
            length = 20,
            height = 5
        )
        val expected = mockProduct(1, 1, "Laptop")
        coEvery {
            productRepository.create(
                categoryId = 1,
                title = "Laptop",
                description = "Gaming laptop",
                width = 30,
                length = 20,
                height = 5,
                userId = 1
            )
        } returns expected
        initService()

        val result = service.createProduct(dto, 1)

        assertEquals("Laptop", result.title)
        coVerify {
            productRepository.create(
                categoryId = 1,
                title = "Laptop",
                description = "Gaming laptop",
                width = 30,
                length = 20,
                height = 5,
                userId = 1
            )
        }
    }

    @Test
    fun `createProduct should throw IllegalArgumentException when title is blank`() = runTest {
        val dto = CreateProductRequest(
            categoryId = 1,
            title = "   ",
            description = null,
            width = 30,
            length = 20,
            height = 5
        )
        initService()

        try {
            service.createProduct(dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Title must not be blank", e.message)
        }
        coVerify(exactly = 0) { productRepository.create(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateProductById should update product when exists`() = runTest {
        val dto = UpdateProductRequest(
            categoryId = 2,
            title = "Updated Laptop",
            description = "Updated description",
            width = 35,
            length = 25,
            height = 6
        )
        val expected = mockProduct(1, 2, "Updated Laptop", "Updated description")
        coEvery {
            productRepository.update(
                id = 1,
                categoryId = 2,
                title = "Updated Laptop",
                description = "Updated description",
                width = 35,
                length = 25,
                height = 6,
                userId = 1
            )
        } returns expected
        initService()

        val result = service.updateProductById(1, dto, 1)

        assertEquals("Updated Laptop", result.title)
        coVerify {
            productRepository.update(
                id = 1,
                categoryId = 2,
                title = "Updated Laptop",
                description = "Updated description",
                width = 35,
                length = 25,
                height = 6,
                userId = 1
            )
        }
    }

    @Test
    fun `updateProductById should throw IllegalArgumentException when title is blank`() = runTest {
        val dto = UpdateProductRequest(categoryId = null, title = "   ", description = null, width = null, length = null, height = null)
        initService()

        try {
            service.updateProductById(1, dto, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Title must not be blank", e.message)
        }
        coVerify(exactly = 0) { productRepository.update(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateProductById should throw NoSuchElementException when not found`() = runTest {
        val dto = UpdateProductRequest(categoryId = null, title = "New Title", description = null, width = null, length = null, height = null)
        coEvery {
            productRepository.update(
                id = 1,
                categoryId = null,
                title = "New Title",
                description = null,
                width = null,
                length = null,
                height = null,
                userId = 1
            )
        } returns null
        initService()

        try {
            service.updateProductById(1, dto, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `deleteProductById should delete product when exists and not used`() = runTest {
        coEvery { productRepository.hasAccess(1, 1) } returns true
        coEvery { productRepository.existsInSupplies(1) } returns false
        coEvery { productRepository.existsInWarehouses(1) } returns false
        coEvery { productRepository.delete(1, 1) } returns true
        initService()

        service.deleteProductById(1, 1)

        coVerify { productRepository.hasAccess(1, 1) }
        coVerify { productRepository.existsInSupplies(1) }
        coVerify { productRepository.existsInWarehouses(1) }
        coVerify { productRepository.delete(1, 1) }
    }

    @Test
    fun `deleteProductById should throw NoSuchElementException when no access`() = runTest {
        coEvery { productRepository.hasAccess(1, 1) } returns false
        initService()

        try {
            service.deleteProductById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
        coVerify(exactly = 0) { productRepository.delete(any(), any()) }
    }

    @Test
    fun `deleteProductById should throw IllegalArgumentException when used in supplies`() = runTest {
        coEvery { productRepository.hasAccess(1, 1) } returns true
        coEvery { productRepository.existsInSupplies(1) } returns true
        initService()

        try {
            service.deleteProductById(1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot delete product that is used in supplies", e.message)
        }
        coVerify(exactly = 0) { productRepository.delete(any(), any()) }
    }

    @Test
    fun `deleteProductById should throw IllegalArgumentException when on warehouse`() = runTest {
        coEvery { productRepository.hasAccess(1, 1) } returns true
        coEvery { productRepository.existsInSupplies(1) } returns false
        coEvery { productRepository.existsInWarehouses(1) } returns true
        initService()

        try {
            service.deleteProductById(1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot delete product that is on warehouse", e.message)
        }
        coVerify(exactly = 0) { productRepository.delete(any(), any()) }
    }
}
