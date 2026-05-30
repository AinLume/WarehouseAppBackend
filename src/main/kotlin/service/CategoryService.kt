package service

import domain.model.Category
import domain.repository.CategoryRepository

class CategoryService(private val repository: CategoryRepository) {

    suspend fun getAllCategories(userId: Int): List<Category> =
        repository.findAllByUserId(userId)

    suspend fun getCategoryById(id: Int, userId: Int): Category =
        repository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Category $id not found")

    suspend fun createCategory(title: String, userId: Int): Category {

        val trimmedTitle = title.trim()

        if (trimmedTitle.isBlank())
            throw IllegalArgumentException("Title must not be blank")
        if (repository.existsByTitle(trimmedTitle))
            throw IllegalArgumentException("Category with title '$trimmedTitle' already exists")

        return repository.create(trimmedTitle, userId)
    }

    suspend fun updateCategoryById(id: Int, title: String, userId: Int): Category {

        val trimmedTitle = title.trim()

        if (trimmedTitle.isBlank())
            throw IllegalArgumentException("Title must not be blank")
        if (repository.existsByTitle(trimmedTitle))
            throw IllegalArgumentException("Category with title '$trimmedTitle' already exists")

        return repository.update(id, trimmedTitle, userId)
            ?: throw NoSuchElementException("Category $id not found")
    }

    suspend fun deleteCategoryById(id: Int, userId: Int) {
        if (!repository.hasAccess(id, userId))
            throw NoSuchElementException("Category $id not found")
        if (repository.hasProducts(id))
            throw IllegalArgumentException("Cannot delete category with products")

        val deleted = repository.delete(id, userId)
        if (!deleted) throw NoSuchElementException("Category $id not found")
    }
}
