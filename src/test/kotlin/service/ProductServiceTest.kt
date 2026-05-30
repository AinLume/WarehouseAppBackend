package service

import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductServiceTest : BaseServiceTest() {

    // getAllProducts
    @Test
    fun getAllProducts_shouldReturnAllProducts() = runTest {
        coEvery { productRepository.findAll(null, null) } returns testProducts

        val result = productService.getAllProducts(null, null, 1)

        assertEquals(testProducts, result)
        coVerify(exactly = 1) { productRepository.findAll(null, null) }
    }

    @Test
    fun getAllProducts_shouldReturnFilteredByCategory() = runTest {
        coEvery { productRepository.findAll(1, null) } returns listOf(testProduct)

        val result = productService.getAllProducts(1, null, 1)

        assertEquals(1, result.size)
        assertEquals(testProduct, result[0])
    }

    @Test
    fun getAllProducts_shouldReturnEmptyList() = runTest {
        coEvery { productRepository.findAll(null, null) } returns emptyList()

        val result = productService.getAllProducts(null, null, 1)

        assertTrue(result.isEmpty())
    }

    // getProductById
    @Test
    fun getProductById_shouldReturnProduct() = runTest {
        coEvery { productRepository.findById(1L) } returns testProduct

        val result = productService.getProductById(1L)

        assertEquals(testProduct, result)
    }

    @Test
    fun getProductById_shouldThrowWhenNotFound() = runTest {
        coEvery { productRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            productService.getProductById(99L)
        }
    }

    // createProduct

    @Test
    fun createProduct_shouldReturnCreatedProduct() = runTest {
        coEvery { categoryRepository.findById(1) } returns testCategory
        coEvery {
            productRepository.create(1, "Laptop", "Gaming laptop")
        } returns testProduct

        val result = productService.createProduct(createProductRequest)

        assertEquals(testProduct, result)
        coVerify(exactly = 1) { productRepository.create(1, "Laptop", "Gaming laptop") }
    }

    @Test
    fun createProduct_shouldThrowWhenTitleIsBlank() = runTest {
        val request = createProductRequest.copy(title = "   ")

        assertFailsWith<IllegalArgumentException> {
            productService.createProduct(request)
        }
        coVerify(exactly = 0) { productRepository.create(any(), any(), any()) }
    }

    @Test
    fun createProduct_shouldThrowWhenCategoryNotFound() = runTest {
        val request = createProductRequest.copy(categoryId = 99)
        coEvery { categoryRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            productService.createProduct(request)
        }
        coVerify(exactly = 0) { productRepository.create(any(), any(), any()) }
    }

    @Test
    fun createProduct_shouldTrimTitleBeforeSaving() = runTest {
        val request = createProductRequest.copy(title = "  Laptop  ")
        coEvery { categoryRepository.findById(1) } returns testCategory
        coEvery { productRepository.create(1, "Laptop", any()) } returns testProduct

        productService.createProduct(request)

        coVerify { productRepository.create(1, "Laptop", any()) }
    }

    @Test
    fun createProduct_shouldCreateWithNullDescription() = runTest {
        val request = createProductRequest.copy(description = null)
        val productNoDesc = testProduct.copy(description = null)
        coEvery { categoryRepository.findById(1) } returns testCategory
        coEvery { productRepository.create(1, "Laptop", null) } returns productNoDesc

        val result = productService.createProduct(request)

        assertNull(result.description)
    }

    // updateProductById

    @Test
    fun updateProductById_shouldReturnUpdatedProduct() = runTest {
        val updated = testProduct.copy(title = "New Laptop")
        coEvery { productRepository.findById(1L) } returns testProduct
        coEvery { productRepository.update(1L, null, "New Laptop", null) } returns updated

        val result = productService.updateProductById(1L, updateProductRequest)

        assertEquals("New Laptop", result.title)
    }

    @Test
    fun updateProductById_shouldThrowWhenProductNotFound() = runTest {
        coEvery { productRepository.findById(99L) } returns null

        assertFailsWith<NoSuchElementException> {
            productService.updateProductById(99L, updateProductRequest)
        }
    }

    @Test
    fun updateProductById_shouldThrowWhenNewCategoryNotFound() = runTest {
        val request = updateProductRequest.copy(categoryId = 99)
        coEvery { productRepository.findById(1L) } returns testProduct
        coEvery { categoryRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            productService.updateProductById(1L, request)
        }
    }

    @Test
    fun updateProductById_shouldThrowWhenTitleIsBlank() = runTest {
        val request = updateProductRequest.copy(title = "   ")
        coEvery { productRepository.findById(1L) } returns testProduct

        assertFailsWith<IllegalArgumentException> {
            productService.updateProductById(1L, request)
        }
    }

    @Test
    fun updateProductById_shouldUpdateOnlyProvidedFields() = runTest {
        val request = updateProductRequest.copy(title = null, description = "New desc")
        val updated = testProduct.copy(description = "New desc")
        coEvery { productRepository.findById(1L) } returns testProduct
        coEvery { productRepository.update(1L, null, null, "New desc") } returns updated

        val result = productService.updateProductById(1L, request)

        assertEquals("New desc", result.description)
        assertEquals(testProduct.title, result.title)
    }

    // deleteProductById

    @Test
    fun deleteProductById_shouldDeleteSuccessfully() = runTest {
        coEvery { productRepository.existsInSupplies(1L) } returns false
        coEvery { productRepository.existsInWarehouses(1L) } returns false
        coEvery { productRepository.delete(1L) } returns true

        productService.deleteProductById(1L)

        coVerify(exactly = 1) { productRepository.delete(1L) }
    }

    @Test
    fun deleteProductById_shouldThrowWhenUsedInSupplies() = runTest {
        coEvery { productRepository.existsInSupplies(1L) } returns true

        assertFailsWith<IllegalArgumentException> {
            productService.deleteProductById(1L)
        }
        coVerify(exactly = 0) { productRepository.delete(any()) }
    }

    @Test
    fun deleteProductById_shouldThrowWhenExistsInWarehouses() = runTest {
        coEvery { productRepository.existsInSupplies(1L) } returns false
        coEvery { productRepository.existsInWarehouses(1L) } returns true

        assertFailsWith<IllegalArgumentException> {
            productService.deleteProductById(1L)
        }
        coVerify(exactly = 0) { productRepository.delete(any()) }
    }

    @Test
    fun deleteProductById_shouldThrowWhenNotFound() = runTest {
        coEvery { productRepository.existsInSupplies(1L) } returns false
        coEvery { productRepository.existsInWarehouses(1L) } returns false
        coEvery { productRepository.delete(1L) } returns false

        assertFailsWith<NoSuchElementException> {
            productService.deleteProductById(1L)
        }
    }
}