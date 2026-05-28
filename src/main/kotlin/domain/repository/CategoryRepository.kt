package domain.repository

import domain.model.Category

interface CategoryRepository {
    suspend fun findAll(): List<Category>
    suspend fun findById(id: Int): Category?
    suspend fun findAllByUserId(userId: Int): List<Category>
    suspend fun findByIdAndUserId(id: Int, userId: Int): Category?
    suspend fun create(title: String, userId: Int): Category
    suspend fun update(id: Int, title: String, userId: Int): Category?
    suspend fun delete(id: Int, userId: Int): Boolean
    suspend fun existsByTitle(title: String): Boolean
    suspend fun hasProducts(id: Int): Boolean
    suspend fun hasAccess(categoryId: Int, userId: Int): Boolean
}