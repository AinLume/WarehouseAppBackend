package domain.repository

import domain.model.Category

interface CategoryRepository {
    suspend fun findAll(): List<Category>
    suspend fun findById(id: Int): Category?
    suspend fun create(title: String): Category
    suspend fun update(id: Int, title: String): Category?
    suspend fun delete(id: Int): Boolean
    suspend fun existsByTitle(title: String): Boolean
    suspend fun hasProducts(id: Int): Boolean
}