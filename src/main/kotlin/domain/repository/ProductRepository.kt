package domain.repository

import domain.model.Product
import domain.model.ProductEx

interface ProductRepository {
    suspend fun findAll(categoryId: Int? = null): List<ProductEx>
    suspend fun findById(id: Long): Product?
    suspend fun create(
        categoryId: Int,
        title: String,
        description: String?,
        width: Int,
        length: Int,
        height: Int
    ): Product
    suspend fun update(
        id: Long,
        categoryId: Int?,
        title: String?,
        description: String?,
        width: Int?,
        length: Int?,
        height: Int?
    ): Product?
    suspend fun delete(id: Long): Boolean
    suspend fun existsInSupplies(id: Long): Boolean
    suspend fun existsInWarehouses(id: Long): Boolean
}