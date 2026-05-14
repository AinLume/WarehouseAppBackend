package service

import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.*

class CategoryServiceTest : BaseServiceTest() {

    // getAllCategories
    @Test
    fun getAllCategories_shouldReturnAllCategories() = runTest {
        coEvery { categoryRepository.findAll() } returns testCategories

        val result = categoryService.getAllCategories()

        assertEquals(testCategories, result)
        coVerify(exactly = 1) { categoryRepository.findAll() }
    }

    @Test
    fun getAllCategories_shouldReturnEmptyList() = runTest {
        coEvery { categoryRepository.findAll() } returns emptyList()

        val result = categoryService.getAllCategories()

        assertTrue(result.isEmpty())
    }

    // getCategoryById
    @Test
    fun getCategoryById_shouldReturnCategory() = runTest {
        coEvery { categoryRepository.findById(1) } returns testCategory

        val result = categoryService.getCategoryById(1)

        assertEquals(testCategory, result)
    }

    @Test
    fun getCategoryById_shouldThrowNoSuchElementException() = runTest {
        coEvery { categoryRepository.findById(99) } returns null

        assertFailsWith<NoSuchElementException> {
            categoryService.getCategoryById(99)
        }
    }

    // createCategory
    @Test
    fun createCategory_shouldReturnCreatedCategory() = runTest {
        coEvery { categoryRepository.existsByTitle("Electronics") } returns false
        coEvery { categoryRepository.create("Electronics") } returns testCategory

        val result = categoryService.createCategory("Electronics")

        assertEquals(testCategory.title, result.title)
        coVerify(exactly = 1) { categoryRepository.create("Electronics") }
    }

    @Test
    fun createCategory_shouldTrimTitleBeforeSaving() = runTest {
        coEvery { categoryRepository.existsByTitle("Electronics") } returns false
        coEvery { categoryRepository.create("Electronics") } returns testCategory

        categoryService.createCategory("  Electronics  ")

        coVerify { categoryRepository.create("Electronics") }
    }

    @Test
    fun createCategory_shouldThrowWhenTitleIsBlank() = runTest {
        assertFailsWith<IllegalArgumentException> {
            categoryService.createCategory("   ")
        }
        coVerify(exactly = 0) { categoryRepository.create(any()) }
    }

    @Test
    fun createCategory_shouldThrowWhenTitleAlreadyExists() = runTest {
        coEvery { categoryRepository.existsByTitle("Electronics") } returns true

        assertFailsWith<IllegalArgumentException> {
            categoryService.createCategory("Electronics")
        }
        coVerify(exactly = 0) { categoryRepository.create(any()) }
    }

    // updateCategoryById
    @Test
    fun updateCategoryById_shouldReturnUpdatedCategory() = runTest {
        val updated = testCategory.copy(title = "New Name")
        coEvery { categoryRepository.existsByTitle("New Name") } returns false
        coEvery { categoryRepository.update(1, "New Name") } returns updated

        val result = categoryService.updateCategoryById(1, "New Name")

        assertEquals("New Name", result.title)
        coVerify(exactly = 1) { categoryRepository.update(1, "New Name") }
    }

    @Test
    fun updateCategoryById_shouldTrimTitleBeforeSaving() = runTest {
        val updated = testCategory.copy(title = "New Name")
        coEvery { categoryRepository.existsByTitle("New Name") } returns false
        coEvery { categoryRepository.update(1, "New Name") } returns updated

        categoryService.updateCategoryById(1, "  New Name  ")

        coVerify { categoryRepository.update(1, "New Name") }
    }

    @Test
    fun updateCategoryById_shouldThrowWhenTitleIsBlank() = runTest {
        assertFailsWith<IllegalArgumentException> {
            categoryService.updateCategoryById(1, "   ")
        }
        coVerify(exactly = 0) { categoryRepository.update(any(), any()) }
    }

    @Test
    fun updateCategoryById_shouldThrowWhenTitleAlreadyExists() = runTest {
        coEvery { categoryRepository.existsByTitle("Electronics") } returns true

        assertFailsWith<IllegalArgumentException> {
            categoryService.updateCategoryById(1, "Electronics")
        }
        coVerify(exactly = 0) { categoryRepository.update(any(), any()) }
    }

    @Test
    fun updateCategoryById_shouldThrowWhenCategoryNotFound() = runTest {
        coEvery { categoryRepository.existsByTitle("New Name") } returns false
        coEvery { categoryRepository.update(99, "New Name") } returns null

        assertFailsWith<NoSuchElementException> {
            categoryService.updateCategoryById(99, "New Name")
        }
    }

    // deleteCategoryById
    @Test
    fun deleteCategoryById_shouldDeleteSuccessfully() = runTest {
        coEvery { categoryRepository.hasProducts(1) } returns false
        coEvery { categoryRepository.delete(1) } returns true

        categoryService.deleteCategoryById(1)

        coVerify(exactly = 1) { categoryRepository.delete(1) }
    }

    @Test
    fun deleteCategoryById_shouldThrowWhenCategoryHasProducts() = runTest {
        coEvery { categoryRepository.hasProducts(1) } returns true

        assertFailsWith<IllegalArgumentException> {
            categoryService.deleteCategoryById(1)
        }
        coVerify(exactly = 0) { categoryRepository.delete(any()) }
    }

    @Test
    fun deleteCategoryById_shouldThrowWhenCategoryNotFound() = runTest {
        coEvery { categoryRepository.hasProducts(99) } returns false
        coEvery { categoryRepository.delete(99) } returns false

        assertFailsWith<NoSuchElementException> {
            categoryService.deleteCategoryById(99)
        }
    }
}