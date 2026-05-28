package data.repository

import data.table.UserWarehouseTable
import data.table.WarehouseProductTable
import data.table.WarehouseSupplyTable
import data.table.WarehouseTable
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import utils.dbQuery

class WarehouseRepositoryImpl : WarehouseRepository {

    private fun rowToWarehouse(row: ResultRow) = Warehouse(
        warehouseId = row[WarehouseTable.warehouseId],
        title = row[WarehouseTable.title],
        address = row[WarehouseTable.address],
        capacity = row[WarehouseTable.capacity],
        width = row[WarehouseTable.width],
        length = row[WarehouseTable.length],
        height = row[WarehouseTable.height]
    )

    override suspend fun findAll(): List<Warehouse> = dbQuery {
        WarehouseTable.selectAll().map(::rowToWarehouse)
    }

    override suspend fun findById(id: Int): Warehouse? = dbQuery {
        WarehouseTable
            .selectAll()
            .where { WarehouseTable.warehouseId eq id }
            .map(::rowToWarehouse)
            .singleOrNull()
    }

    override suspend fun findByIdAndUserId(id: Int, userId: Int): Warehouse? = dbQuery {
        (WarehouseTable innerJoin UserWarehouseTable)
            .selectAll()
            .where {
                (WarehouseTable.warehouseId eq id) and
                (UserWarehouseTable.userId eq userId)
            }
            .map(::rowToWarehouse)
            .singleOrNull()
    }

    override suspend fun findAllByUserId(userId: Int): List<Warehouse> = dbQuery {
        (WarehouseTable innerJoin UserWarehouseTable)
            .selectAll()
            .where { UserWarehouseTable.userId eq userId }
            .map(::rowToWarehouse)
    }

    override suspend fun create(
        title: String?,
        address: String,
        capacity: Int?,
        width: Long,
        length: Long,
        height: Long,
        userId: Int
    ): Warehouse = dbQuery {
        val id = WarehouseTable.insert {
            it[WarehouseTable.title] = title
            it[WarehouseTable.address] = address
            it[WarehouseTable.capacity] = capacity
            it[WarehouseTable.width] = width
            it[WarehouseTable.length] = length
            it[WarehouseTable.height] = height
        } get WarehouseTable.warehouseId

        UserWarehouseTable.insert {
            it[UserWarehouseTable.userId] = userId
            it[UserWarehouseTable.warehouseId] = id
        }

        Warehouse(id, title, address, capacity, width, length, height)
    }

    override suspend fun update(
        id: Int,
        title: String?,
        address: String?,
        capacity: Int?,
        width: Long?,
        length: Long?,
        height: Long?,
        userId: Int
    ): Warehouse? = dbQuery {
        if (!hasAccess(id, userId)) return@dbQuery null

        val updated = WarehouseTable.update(
            where = { WarehouseTable.warehouseId eq id }
        ) {
            title?.let { v -> it[WarehouseTable.title] = v }
            address?.let { v -> it[WarehouseTable.address] = v }
            capacity?.let { v -> it[WarehouseTable.capacity] = v }
            width?.let { v -> it[WarehouseTable.width] = v }
            length?.let { v -> it[WarehouseTable.length] = v }
            height?.let { v -> it[WarehouseTable.height] = v }
        }
        if (updated == 0) null else findById(id)
    }

    override suspend fun delete(id: Int, userId: Int): Boolean = dbQuery {
        val hasAccess = hasAccess(id, userId)
        if (!hasAccess) return@dbQuery false

        UserWarehouseTable.deleteWhere {
            (UserWarehouseTable.warehouseId eq id) and
            (UserWarehouseTable.userId eq userId)
        }

        WarehouseTable.deleteWhere { warehouseId eq id } > 0
    }

    override suspend fun hasProducts(id: Int): Boolean = dbQuery {
        WarehouseProductTable
            .selectAll()
            .where { WarehouseProductTable.warehouseId eq id }
            .count() > 0
    }

    override suspend fun findBySupplyId(supplyId: Long): Warehouse? = dbQuery {
        (WarehouseTable innerJoin WarehouseSupplyTable)
            .selectAll()
            .where { WarehouseSupplyTable.supplyId eq supplyId }
            .map(::rowToWarehouse)
            .singleOrNull()
    }

    override suspend fun hasAccess(warehouseId: Int, userId: Int): Boolean = dbQuery {
        UserWarehouseTable
            .selectAll()
            .where {
                (UserWarehouseTable.warehouseId eq warehouseId) and
                (UserWarehouseTable.userId eq userId)
            }
            .count() > 0
    }
}