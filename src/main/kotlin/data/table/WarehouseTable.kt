package data.table

import org.jetbrains.exposed.sql.Table

object WarehouseTable: Table("warehouse") {
    val warehouseId = integer("warehouse_id").autoIncrement()
    val title = varchar("title", 100).nullable()
    val address = text("address")
    val capacity = integer("capacity").nullable()
    val width = long("width")
    val length = long("length")
    val height = long("height")

    override val primaryKey = PrimaryKey(warehouseId)
}