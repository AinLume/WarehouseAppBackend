package service

import api.dto.CreateProductRequest
import api.dto.UpdateProductRequest
import domain.model.Product
import domain.model.ProductEx
import domain.repository.ProductRepository

class ProductService(
    private val repository: ProductRepository
) {

    suspend fun getAllProducts(categoryId: Int?, userId: Int): List<ProductEx> =
        repository.findAllByUserId(userId, categoryId)

    suspend fun getProductById(id: Long, userId: Int): Product =
        repository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Product $id not found")

    suspend fun createProduct(dto: CreateProductRequest, userId: Int): Product {
        if (dto.title.isBlank())
            throw IllegalArgumentException("Title must not be blank")

        return repository.create(
            categoryId = dto.categoryId,
            title = dto.title.trim(),
            description = dto.description?.trim(),
            width = dto.width,
            length = dto.length,
            height = dto.height,
            userId = userId
        )
    }

    suspend fun updateProductById(id: Long, dto: UpdateProductRequest, userId: Int): Product {
        if (dto.title != null && dto.title.isBlank())
            throw IllegalArgumentException("Title must not be blank")

        return repository.update(
            id = id,
            categoryId = dto.categoryId,
            title = dto.title?.trim(),
            description = dto.description?.trim(),
            width = dto.width,
            length = dto.length,
            height = dto.height,
            userId = userId
        ) ?: throw NoSuchElementException("Product $id not found")
    }

    suspend fun deleteProductById(id: Long, userId: Int) {
        if (!repository.hasAccess(id, userId))
            throw NoSuchElementException("Product $id not found")
        if (repository.existsInSupplies(id))
            throw IllegalArgumentException("Cannot delete product that is used in supplies")
        if (repository.existsInWarehouses(id))
            throw IllegalArgumentException("Cannot delete product that is on warehouse")

        val deleted = repository.delete(id, userId)
        if (!deleted) throw NoSuchElementException("Product $id not found")
    }
}
