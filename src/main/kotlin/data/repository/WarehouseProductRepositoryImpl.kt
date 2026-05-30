package data.repository

import data.table.CategoryTable
import data.table.ProductTable
import data.table.UserWarehouseTable
import data.table.WarehouseProductTable
import domain.model.Category
import domain.model.Product
import domain.model.SupplyProduct
import domain.model.WarehouseProduct
import domain.model.WarehouseProductDetail
import domain.repository.WarehouseProductRepository
import domain.repository.WarehouseStats
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import utils.dbQuery

class WarehouseProductRepositoryImpl : WarehouseProductRepository {

    private val log = LoggerFactory.getLogger("WarehouseProductRepository")

    private fun rowToWarehouseProduct(row: ResultRow) = WarehouseProduct(
        warehouseProductId = row[WarehouseProductTable.warehouseProductId],
        warehouseId = row[WarehouseProductTable.warehouseId],
        productId = row[WarehouseProductTable.productId],
        quantity = row[WarehouseProductTable.quantity],
        totalPrice = row[WarehouseProductTable.totalPrice]
    )

    override suspend fun findByWarehouse(
        warehouseId: Int
    ): List<WarehouseProduct> = dbQuery {

        WarehouseProductTable
            .selectAll()
            .where { WarehouseProductTable.warehouseId eq warehouseId }
            .map(::rowToWarehouseProduct)
    }

    override suspend fun findByWarehouseWithDetails(warehouseId: Int): List<WarehouseProductDetail> = dbQuery {
        (WarehouseProductTable innerJoin ProductTable)
            .selectAll()
            .where { WarehouseProductTable.warehouseId eq warehouseId }
            .map { row ->
                WarehouseProductDetail(
                    warehouseProductId = row[WarehouseProductTable.warehouseProductId],
                    quantity = row[WarehouseProductTable.quantity],
                    product = Product(
                        productId = row[ProductTable.productId],
                        categoryId = row[ProductTable.categoryId],
                        title = row[ProductTable.title],
                        description = row[ProductTable.description],
                        width = row[ProductTable.width],
                        length = row[ProductTable.length],
                        height = row[ProductTable.height]
                    )
                )
            }
    }

    override suspend fun findByWarehouseGroupedByCategory(
        warehouseId: Int
    ): Map<Category, List<WarehouseProductDetail>> = dbQuery {

        (WarehouseProductTable innerJoin ProductTable innerJoin CategoryTable)
            .selectAll()
            .where { WarehouseProductTable.warehouseId eq warehouseId }
            .map { row ->
                val category = Category(
                    categoryId = row[CategoryTable.categoryId],
                    title = row[CategoryTable.title]
                )
                category to WarehouseProductDetail(
                    warehouseProductId = row[WarehouseProductTable.warehouseProductId],
                    quantity = row[WarehouseProductTable.quantity],
                    product = Product(
                        productId = row[ProductTable.productId],
                        categoryId = row[ProductTable.categoryId],
                        title = row[ProductTable.title],
                        description = row[ProductTable.description],
                        width = row[ProductTable.width],
                        length = row[ProductTable.length],
                        height = row[ProductTable.height]
                    )
                )
            }
            .groupBy({ it.first }, { it.second })
    }

    override suspend fun findByWarehouseIdAndProductId(
        warehouseId: Int,
        productId: Long
    ): WarehouseProduct? = dbQuery {

        WarehouseProductTable
            .selectAll()
            .where {
                WarehouseProductTable.warehouseId eq warehouseId and
                (WarehouseProductTable.productId eq productId)
            }
            .map(::rowToWarehouseProduct)
            .singleOrNull()
    }

    override suspend fun upsert(
        warehouseId: Int,
        productId: Long,
        quantity: Int,
        unitPrice: Long
    ): WarehouseProduct = dbQuery {

        val existed = findByWarehouseIdAndProductId(warehouseId, productId)

        if (existed != null) {
            val newQuantity = existed.quantity + quantity
            val newTotalPrice = newQuantity * unitPrice

            WarehouseProductTable.update(
                where = {
                    WarehouseProductTable.warehouseId eq warehouseId and
                    (WarehouseProductTable.productId eq productId)
                }
            ) {
                it[WarehouseProductTable.quantity] = newQuantity
                it[WarehouseProductTable.totalPrice] = newTotalPrice
            }

            WarehouseProduct(existed.warehouseProductId, warehouseId, productId, quantity, unitPrice )
        }
        else {
            val totalPrice = quantity * unitPrice

            val id = WarehouseProductTable.insert {
                it[WarehouseProductTable.warehouseId] = warehouseId
                it[WarehouseProductTable.productId] = productId
                it[WarehouseProductTable.quantity] = quantity
                it[WarehouseProductTable.totalPrice] = totalPrice
            } get WarehouseProductTable.warehouseProductId

            WarehouseProduct(id, warehouseId, productId, quantity, totalPrice)
        }
    }

    override suspend fun getTotalStats(warehouseId: Int): WarehouseStats = dbQuery {
        val rows = WarehouseProductTable
            .selectAll()
            .where { WarehouseProductTable.warehouseId eq warehouseId }
            .toList()

        WarehouseStats(
            totalQuantity = rows.sumOf { it[WarehouseProductTable.quantity] },
            totalPrice = rows.sumOf { it[WarehouseProductTable.totalPrice] },
            uniqueProducts = rows.count()
        )
    }

    override suspend fun upsertAll(warehouseId: Int, products: List<SupplyProduct>) : Unit = dbQuery {
        val sql = """
            INSERT INTO warehouse_product (warehouse_id, product_id, quantity, total_price)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (warehouse_id, product_id)
            DO UPDATE SET
                quantity = warehouse_product.quantity + EXCLUDED.quantity,
                total_price = (warehouse_product.quantity + EXCLUDED.quantity) * (EXCLUDED.total_price / EXCLUDED.quantity)
            """.trimIndent()

        org.jetbrains.exposed.sql.transactions.TransactionManager
            .current().connection.prepareStatement(sql, false).also { stmt ->
                products.forEach { product ->
                    stmt.fillParameters(listOf(
                        Pair(IntegerColumnType(), warehouseId),
                        Pair(LongColumnType(), product.productId),
                        Pair(IntegerColumnType(), product.quantity),
                        Pair(LongColumnType(), product.quantity * product.unitPrice)
                    ))
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
    }

    override suspend fun getTotalQuantityAll(): Int = dbQuery {
        WarehouseProductTable.selectAll()
            .sumOf { it[WarehouseProductTable.quantity] }
    }

    override suspend fun getTotalPriceAll(): Long = dbQuery {
        WarehouseProductTable.selectAll()
            .sumOf { it[WarehouseProductTable.totalPrice] }
    }

    override suspend fun getTotalQuantityByUserId(userId: Int): Int = dbQuery {
        val userWarehouseIds = UserWarehouseTable
            .selectAll()
            .where { UserWarehouseTable.userId eq userId }
            .map { it[UserWarehouseTable.warehouseId] }

        if (userWarehouseIds.isEmpty()) return@dbQuery 0

        WarehouseProductTable
            .selectAll()
            .where { WarehouseProductTable.warehouseId inList userWarehouseIds }
            .sumOf { it[WarehouseProductTable.quantity] }
    }

    override suspend fun getTotalPriceByUserId(userId: Int): Long = dbQuery {
        val userWarehouseIds = UserWarehouseTable
            .selectAll()
            .where { UserWarehouseTable.userId eq userId }
            .map { it[UserWarehouseTable.warehouseId] }

        if (userWarehouseIds.isEmpty()) return@dbQuery 0L

        WarehouseProductTable
            .selectAll()
            .where { WarehouseProductTable.warehouseId inList userWarehouseIds }
            .sumOf { it[WarehouseProductTable.totalPrice] }
    }
}