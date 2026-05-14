package service

import domain.model.Category
import domain.repository.CategoryRepository

class CategoryService(private val repository: CategoryRepository) {

    suspend fun getAllCategories(): List<Category> =
        repository.findAll()

    suspend fun getCategoryById(id: Int): Category =
        repository.findById(id)
            ?: throw NoSuchElementException("Category $id not found")

    suspend fun createCategory(title: String): Category {

        val trimmedTitle = title.trim()

        if (trimmedTitle.isBlank())
            throw IllegalArgumentException("Title must not be blank")
        if (repository.existsByTitle(trimmedTitle))
            throw IllegalArgumentException("Category with title '$trimmedTitle' already exists")

        return repository.create(trimmedTitle)
    }

    suspend fun updateCategoryById(id: Int, title: String): Category {

        val trimmedTitle = title.trim()

        if (trimmedTitle.isBlank())
            throw IllegalArgumentException("Title must not be blank")
        if (repository.existsByTitle(trimmedTitle))
            throw IllegalArgumentException("Category with title '$trimmedTitle' already exists")

        return repository.update(id, trimmedTitle)
            ?: throw NoSuchElementException("Category $id not found")
    }

    suspend fun deleteCategoryById(id: Int) {
        if (repository.hasProducts(id))
            throw IllegalArgumentException("Cannot delete category with products")

        val deleted = repository.delete(id)
        if (!deleted) throw NoSuchElementException("Category $id not found")
    }
}