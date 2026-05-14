package data.repository

import data.table.CategoryTable
import data.table.ProductTable
import data.table.SupplyProductTable
import data.table.WarehouseProductTable
import domain.model.Product
import domain.model.ProductEx
import domain.repository.ProductRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import utils.dbQuery

class ProductRepositoryImpl : ProductRepository {

    private fun rowToProduct(row: ResultRow) = Product(
        productId = row[ProductTable.productId],
        categoryId = row[ProductTable.categoryId],
        title = row[ProductTable.title],
        description = row[ProductTable.description],
        width = row[ProductTable.width],
        length = row[ProductTable.length],
        height = row[ProductTable.height]
    )

    private fun rowToProductEx(row: ResultRow) = ProductEx(
        productId = row[ProductTable.productId],
        categoryId = row[ProductTable.categoryId],
        categoryTitle = row[CategoryTable.title],
        title = row[ProductTable.title],
        description = row[ProductTable.description],
        width = row[ProductTable.width],
        length = row[ProductTable.length],
        height = row[ProductTable.height]
    )

    override suspend fun findAll(categoryId: Int?): List<ProductEx> = dbQuery {
        (ProductTable innerJoin CategoryTable)
            .selectAll()
            .apply { categoryId?.let { where { ProductTable.categoryId eq it } } }
            .map(::rowToProductEx)
    }

    override suspend fun findById(id: Long): Product? = dbQuery {
        ProductTable
            .selectAll()
            .where { ProductTable.productId eq id }
            .map(::rowToProduct)
            .singleOrNull()
    }

    override suspend fun create(
        categoryId: Int,
        title: String,
        description: String?,
        width: Int,
        length: Int,
        height: Int
    ): Product = dbQuery {

        val id = ProductTable.insert {
            it[ProductTable.categoryId] = categoryId
            it[ProductTable.title] = title
            it[ProductTable.description] = description
            it[ProductTable.width] = width
            it[ProductTable.length] = length
            it[ProductTable.height] = height
        } get ProductTable.productId

        Product(id, categoryId, title, description, width, length, height)
    }

    override suspend fun update(
        id: Long,
        categoryId: Int?,
        title: String?,
        description: String?,
        width: Int?,
        length: Int?,
        height: Int?
    ): Product? = dbQuery {

        val updated = ProductTable.update(
            where = { ProductTable.productId eq id }
        ) {
            categoryId?.let { v -> it[ProductTable.categoryId] = v }
            title?.let { v -> it[ProductTable.title] = v }
            description?.let { v -> it[ProductTable.description] = v }
            width?.let { v -> it[ProductTable.width] = v }
            length?.let { v -> it[ProductTable.length] = v }
            height?.let { v -> it[ProductTable.height] = v }
        }
        if (updated == 0) null else findById(id)
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        ProductTable.deleteWhere { productId eq id } > 0
    }

    override suspend fun existsInSupplies(id: Long): Boolean = dbQuery {
        SupplyProductTable
            .selectAll()
            .where { SupplyProductTable.productId eq id }
            .count() > 0
    }

    override suspend fun existsInWarehouses(id: Long): Boolean = dbQuery {
        WarehouseProductTable
            .selectAll()
            .where { WarehouseProductTable.productId eq id }
            .count() > 0
    }
}