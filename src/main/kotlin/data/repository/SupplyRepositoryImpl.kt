package data.repository

import data.table.SupplyProductTable
import data.table.SupplyTable
import data.table.WarehouseSupplyTable
import data.table.ProductTable
import data.table.SupplierTable
import data.table.WarehouseTable
import data.table.UserWarehouseTable
import domain.model.Product
import domain.model.Supply
import domain.model.SupplyEnriched
import domain.model.SupplyProduct
import domain.model.SupplyProductDetail
import domain.model.SupplyStatus
import domain.repository.SupplyRepository
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import utils.dbQuery
import java.time.OffsetDateTime

class SupplyRepositoryImpl : SupplyRepository {

    private fun rowToSupply(row: ResultRow) = Supply(
        supplyId = row[SupplyTable.supplyId],
        supplierId = row[SupplyTable.supplierId],
        status = SupplyStatus.valueOf(row[SupplyTable.status]),
        totalPrice = row[SupplyTable.totalPrice],
        createdAt = row[SupplyTable.createdAt].toString(),
        updatedAt = row[SupplyTable.updatedAt].toString()
    )

    private fun rowToSupplyEnriched(row: ResultRow) = SupplyEnriched(
        supply = rowToSupply(row),
        warehouseId = row[WarehouseSupplyTable.warehouseId],
        warehouseTitle = row[WarehouseTable.title],
        supplierName = row[SupplierTable.name]
    )

    private fun rowToSupplyProduct(row: ResultRow) = SupplyProduct(
        supplyProductId = row[SupplyProductTable.supplyProductId],
        supplyId = row[SupplyProductTable.supplyId],
        productId = row[SupplyProductTable.productId],
        quantity = row[SupplyProductTable.quantity],
        unitPrice = row[SupplyProductTable.unitPrice]
    )

    override suspend fun findAll(
        warehouseId: Int?,
        status: SupplyStatus?
    ): List<SupplyEnriched> = dbQuery {
        val query = (
                SupplyTable
                        innerJoin WarehouseSupplyTable
                        innerJoin SupplierTable
                        innerJoin WarehouseTable
                )
            .selectAll()

        warehouseId?.let { query.andWhere { WarehouseSupplyTable.warehouseId eq it } }
        status?.let { query.andWhere { SupplyTable.status eq it.name } }

        query.map(::rowToSupplyEnriched)
    }

    override suspend fun findAllByUserId(
        userId: Int,
        warehouseId: Int?,
        status: SupplyStatus?
    ): List<SupplyEnriched> = dbQuery {
        val query = (
                SupplyTable
                        innerJoin WarehouseSupplyTable
                        innerJoin SupplierTable
                        innerJoin WarehouseTable
                        innerJoin UserWarehouseTable
                )
            .selectAll()
            .where { UserWarehouseTable.userId eq userId }

        warehouseId?.let { query.andWhere { WarehouseSupplyTable.warehouseId eq it } }
        status?.let { query.andWhere { SupplyTable.status eq it.name } }

        query.map(::rowToSupplyEnriched)
    }

    private fun findByIdInternal(id: Long): SupplyEnriched? =
        SupplyTable
            .innerJoin(WarehouseSupplyTable,
                onColumn = { SupplyTable.supplyId },
                otherColumn = { WarehouseSupplyTable.supplyId }
            )
            .join(
                SupplierTable,
                JoinType.INNER,
                onColumn = SupplyTable.supplierId,
                otherColumn = SupplierTable.supplierId
            )
            .join(WarehouseTable, JoinType.INNER,
                onColumn = WarehouseSupplyTable.warehouseId,
                otherColumn = WarehouseTable.warehouseId
            )
            .selectAll()
            .where { SupplyTable.supplyId eq id }
            .map(::rowToSupplyEnriched)
            .singleOrNull()

    override suspend fun findById(id: Long): SupplyEnriched? = dbQuery {
        findByIdInternal(id)
    }

    override suspend fun findByIdAndUserId(id: Long, userId: Int): SupplyEnriched? = dbQuery {
        if (!hasAccess(id, userId)) return@dbQuery null
        findByIdInternal(id)
    }

    override suspend fun create(supplierId: Int, warehouseId: Int, userId: Int): SupplyEnriched = dbQuery {
        val now = OffsetDateTime.now()

        val id = SupplyTable.insert {
            it[SupplyTable.supplierId] = supplierId
            it[SupplyTable.status] = SupplyStatus.CREATED.name
            it[SupplyTable.totalPrice] = 0
            it[SupplyTable.createdAt] = now
            it[SupplyTable.updatedAt] = now
        } get SupplyTable.supplyId

        println("Inserted supply id = $id")

        WarehouseSupplyTable.insert {
            it[WarehouseSupplyTable.warehouseId] = warehouseId
            it[WarehouseSupplyTable.supplyId] = id
        }

        println("Inserted warehouse-supply link")

        findByIdInternal(id)!!
    }

    override suspend fun updateStatus(id: Long, status: SupplyStatus): SupplyEnriched? = dbQuery {
        val updated = SupplyTable.update(
            where = { SupplyTable.supplyId eq id }
        ) {
            it[SupplyTable.status] = status.name
            it[SupplyTable.updatedAt] = OffsetDateTime.now()
        }
        if (updated == 0) null else findByIdInternal(id)
    }

    override suspend fun updateTotalPrice(id: Long) = dbQuery {
        val total = SupplyProductTable
            .selectAll()
            .where { SupplyProductTable.supplyId eq id }
            .sumOf { it[SupplyProductTable.unitPrice] * it[SupplyProductTable.quantity] }

        SupplyTable.update(
            where = { SupplyTable.supplyId eq id }
        ) {
            it[SupplyTable.totalPrice] = total
        }
        Unit
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        SupplyTable.deleteWhere { supplyId eq id } > 0
    }

    override suspend fun findProducts(supplyId: Long): List<SupplyProduct> = dbQuery {
        SupplyProductTable
            .selectAll()
            .where { SupplyProductTable.supplyId eq supplyId }
            .map(::rowToSupplyProduct)
    }

    override suspend fun findProductsDetails(supplyId: Long): List<SupplyProductDetail> = dbQuery {
            (SupplyProductTable innerJoin ProductTable)
                .selectAll()
                .where { SupplyProductTable.supplyId eq supplyId }
                .map { row ->
                    SupplyProductDetail(
                        supplyProductId = row[SupplyProductTable.supplyProductId],
                        quantity = row[SupplyProductTable.quantity],
                        unitPrice = row[SupplyProductTable.unitPrice],
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

    override suspend fun addProduct(
        supplyId: Long,
        productId: Long,
        quantity: Int,
        unitPrice: Long
    ): SupplyProduct = dbQuery {
        val id = SupplyProductTable.insert {
            it[SupplyProductTable.supplyId] = supplyId
            it[SupplyProductTable.productId] = productId
            it[SupplyProductTable.quantity] = quantity
            it[SupplyProductTable.unitPrice] = unitPrice
        } get SupplyProductTable.supplyProductId

        SupplyProduct(
            supplyProductId = id,
            supplyId = supplyId,
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice
        )
    }

    override suspend fun removeProduct(supplyId: Long, productId: Long): Boolean = dbQuery {
        SupplyProductTable.deleteWhere {
            SupplyProductTable.supplyId eq supplyId and
            (SupplyProductTable.productId eq productId)
        } != 0
    }

    override suspend fun countByStatus(): Map<String, Int> = dbQuery {
        SupplyTable.selectAll()
            .map { it[SupplyTable.status] }
            .groupingBy { it }
            .eachCount()
    }

    override suspend fun hasAccess(supplyId: Long, userId: Int): Boolean = dbQuery {
        WarehouseSupplyTable
            .innerJoin(UserWarehouseTable,
                onColumn = { WarehouseSupplyTable.warehouseId },
                otherColumn = { UserWarehouseTable.warehouseId }
            )
            .selectAll()
            .where {
                (WarehouseSupplyTable.supplyId eq supplyId) and
                (UserWarehouseTable.userId eq userId)
            }
            .count() > 0
    }
}
