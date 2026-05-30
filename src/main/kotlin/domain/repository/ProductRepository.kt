package domain.repository

import domain.model.Product
import domain.model.ProductEx

interface ProductRepository {
    suspend fun findAll(categoryId: Int? = null, search: String? = null): List<ProductEx>
    suspend fun findById(id: Long): Product?
    suspend fun findAllByUserId(userId: Int, categoryId: Int? = null, search: String? = null): List<ProductEx>
    suspend fun findByIdAndUserId(id: Long, userId: Int): Product?
    suspend fun create(
        categoryId: Int,
        title: String,
        description: String?,
        width: Int,
        length: Int,
        height: Int,
        userId: Int
    ): Product
    suspend fun update(
        id: Long,
        categoryId: Int?,
        title: String?,
        description: String?,
        width: Int?,
        length: Int?,
        height: Int?,
        userId: Int
    ): Product?
    suspend fun delete(id: Long, userId: Int): Boolean
    suspend fun existsInSupplies(id: Long): Boolean
    suspend fun existsInWarehouses(id: Long): Boolean
    suspend fun hasAccess(productId: Long, userId: Int): Boolean
}