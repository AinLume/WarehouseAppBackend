package service

import api.dto.CreateProductRequest
import api.dto.UpdateProductRequest
import domain.model.Product
import domain.model.ProductEx
import domain.repository.ProductRepository

class ProductService(
    private val repository: ProductRepository,
    private val categoryService: CategoryService
) {

    suspend fun getAllProducts(categoryId: Int?): List<ProductEx> =
        repository.findAll(categoryId)

    suspend fun getProductById(id: Long): Product =
        repository.findById(id)
            ?: throw NoSuchElementException("Product $id not found")

    suspend fun createProduct(dto: CreateProductRequest): Product {
        if (dto.title.isBlank())
            throw IllegalArgumentException("Title must not be blank")

        categoryService.getCategoryById(dto.categoryId)

        return repository.create(
            categoryId = dto.categoryId,
            title = dto.title.trim(),
            description = dto.description?.trim(),
            width = dto.width,
            length = dto.length,
            height = dto.height
        )
    }

    suspend fun updateProductById(id: Long, dto: UpdateProductRequest): Product {
        getProductById(id)

        dto.categoryId?.let {
            categoryService.getCategoryById(dto.categoryId)
        }
        if (dto.title != null && dto.title.isBlank())
            throw IllegalArgumentException("Title must not be blank")

        return repository.update(
            id = id,
            categoryId = dto.categoryId,
            title = dto.title?.trim(),
            description = dto.description?.trim(),
            width = dto.width,
            length = dto.length,
            height = dto.height
        ) ?: throw NoSuchElementException("Product $id not found")
    }

    suspend fun deleteProductById(id: Long) {
        if (repository.existsInSupplies(id))
            throw IllegalArgumentException("Cannot delete product that is used in supplies")
        if (repository.existsInWarehouses(id))
            throw IllegalArgumentException("Cannot delete product that is on warehouse")

        val deleted = repository.delete(id)
        if (!deleted) throw NoSuchElementException("Product $id not found")
    }
}