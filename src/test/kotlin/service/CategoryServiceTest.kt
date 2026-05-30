package service

import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CategoryServiceTest : BaseServiceTest() {

    private lateinit var service: CategoryService

    private fun initService() {
        service = CategoryService(categoryRepository)
    }

    @Test
    fun `getAllCategories should return list of categories`() = runTest {
        val expected = listOf(mockCategory(1, "Electronics"), mockCategory(2, "Furniture"))
        coEvery { categoryRepository.findAllByUserId(1) } returns expected
        initService()

        val result = service.getAllCategories(1)

        assertEquals(2, result.size)
        assertEquals("Electronics", result[0].title)
        coVerify { categoryRepository.findAllByUserId(1) }
    }

    @Test
    fun `getCategoryById should return category when exists and belongs to user`() = runTest {
        val expected = mockCategory(1, "Electronics")
        coEvery { categoryRepository.findByIdAndUserId(1, 1) } returns expected
        initService()

        val result = service.getCategoryById(1, 1)

        assertEquals("Electronics", result.title)
        coVerify { categoryRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `getCategoryById should throw NoSuchElementException when not found`() = runTest {
        coEvery { categoryRepository.findByIdAndUserId(1, 1) } returns null
        initService()

        try {
            service.getCategoryById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Category 1 not found", e.message)
        }
        coVerify { categoryRepository.findByIdAndUserId(1, 1) }
    }

    @Test
    fun `createCategory should create category with valid title`() = runTest {
        val expected = mockCategory(1, "Electronics")
        coEvery { categoryRepository.existsByTitle("Electronics") } returns false
        coEvery { categoryRepository.create("Electronics", 1) } returns expected
        initService()

        val result = service.createCategory("Electronics", 1)

        assertEquals("Electronics", result.title)
        coVerify { categoryRepository.existsByTitle("Electronics") }
        coVerify { categoryRepository.create("Electronics", 1) }
    }

    @Test
    fun `createCategory should throw IllegalArgumentException when title is blank`() = runTest {
        initService()

        try {
            service.createCategory("   ", 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Title must not be blank", e.message)
        }
        coVerify(exactly = 0) { categoryRepository.create(any(), any()) }
    }

    @Test
    fun `createCategory should trim whitespace and create category`() = runTest {
        val expected = mockCategory(1, "Electronics")
        coEvery { categoryRepository.existsByTitle("Electronics") } returns false
        coEvery { categoryRepository.create("Electronics", 1) } returns expected
        initService()

        val result = service.createCategory("  Electronics  ", 1)

        assertEquals("Electronics", result.title)
        coVerify { categoryRepository.create("Electronics", 1) }
    }

    @Test
    fun `createCategory should throw IllegalArgumentException when title already exists`() = runTest {
        coEvery { categoryRepository.existsByTitle("Electronics") } returns true
        initService()

        try {
            service.createCategory("Electronics", 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Category with title 'Electronics' already exists", e.message)
        }
        coVerify { categoryRepository.existsByTitle("Electronics") }
        coVerify(exactly = 0) { categoryRepository.create(any(), any()) }
    }

    @Test
    fun `updateCategoryById should update category when exists and belongs to user`() = runTest {
        val expected = mockCategory(1, "Updated Electronics")
        coEvery { categoryRepository.existsByTitle("Updated Electronics") } returns false
        coEvery { categoryRepository.update(1, "Updated Electronics", 1) } returns expected
        initService()

        val result = service.updateCategoryById(1, "Updated Electronics", 1)

        assertEquals("Updated Electronics", result.title)
        coVerify { categoryRepository.existsByTitle("Updated Electronics") }
        coVerify { categoryRepository.update(1, "Updated Electronics", 1) }
    }

    @Test
    fun `updateCategoryById should throw IllegalArgumentException when title is blank`() = runTest {
        initService()

        try {
            service.updateCategoryById(1, "   ", 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Title must not be blank", e.message)
        }
        coVerify(exactly = 0) { categoryRepository.update(any(), any(), any()) }
    }

    @Test
    fun `updateCategoryById should throw IllegalArgumentException when title already exists`() = runTest {
        coEvery { categoryRepository.existsByTitle("Other Category") } returns true
        initService()

        try {
            service.updateCategoryById(1, "Other Category", 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Category with title 'Other Category' already exists", e.message)
        }
        coVerify { categoryRepository.existsByTitle("Other Category") }
        coVerify(exactly = 0) { categoryRepository.update(any(), any(), any()) }
    }

    @Test
    fun `updateCategoryById should throw NoSuchElementException when not found`() = runTest {
        coEvery { categoryRepository.existsByTitle("New Title") } returns false
        coEvery { categoryRepository.update(1, "New Title", 1) } returns null
        initService()

        try {
            service.updateCategoryById(1, "New Title", 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
    }

    @Test
    fun `deleteCategoryById should delete category when exists and has no products`() = runTest {
        coEvery { categoryRepository.hasAccess(1, 1) } returns true
        coEvery { categoryRepository.hasProducts(1) } returns false
        coEvery { categoryRepository.delete(1, 1) } returns true
        initService()

        service.deleteCategoryById(1, 1)

        coVerify { categoryRepository.hasAccess(1, 1) }
        coVerify { categoryRepository.hasProducts(1) }
        coVerify { categoryRepository.delete(1, 1) }
    }

    @Test
    fun `deleteCategoryById should throw NoSuchElementException when no access`() = runTest {
        coEvery { categoryRepository.hasAccess(1, 1) } returns false
        initService()

        try {
            service.deleteCategoryById(1, 1)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
        }
        coVerify { categoryRepository.hasAccess(1, 1) }
        coVerify(exactly = 0) { categoryRepository.delete(any(), any()) }
    }

    @Test
    fun `deleteCategoryById should throw IllegalArgumentException when has products`() = runTest {
        coEvery { categoryRepository.hasAccess(1, 1) } returns true
        coEvery { categoryRepository.hasProducts(1) } returns true
        initService()

        try {
            service.deleteCategoryById(1, 1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot delete category with products", e.message)
        }
        coVerify { categoryRepository.hasAccess(1, 1) }
        coVerify { categoryRepository.hasProducts(1) }
        coVerify(exactly = 0) { categoryRepository.delete(any(), any()) }
    }
}
