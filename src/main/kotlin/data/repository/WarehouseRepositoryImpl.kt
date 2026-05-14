package data.repository

import data.table.WarehouseProductTable
import data.table.WarehouseSupplyTable
import data.table.WarehouseTable
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    override suspend fun create(
        title: String?,
        address: String,
        capacity: Int?,
        width: Long,
        length: Long,
        height: Long,
    ): Warehouse = dbQuery {
        val id = WarehouseTable.insert {
            it[WarehouseTable.title] = title
            it[WarehouseTable.address] = address
            it[WarehouseTable.capacity] = capacity
            it[WarehouseTable.width] = width
            it[WarehouseTable.length] = length
            it[WarehouseTable.height] = height
        } get WarehouseTable.warehouseId

        Warehouse(id, title, address, capacity, width, length, height)
    }

    override suspend fun update(
        id: Int,
        title: String?,
        address: String?,
        capacity: Int?,
        width: Long?,
        length: Long?,
        height: Long?
    ): Warehouse? = dbQuery {
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

    override suspend fun delete(id: Int): Boolean = dbQuery {
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
}