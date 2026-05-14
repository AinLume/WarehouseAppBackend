package data.table

import org.jetbrains.exposed.sql.Table

object WarehouseSupplyTable : Table("warehouse_supply") {
    val warehouseSupplyId = long("warehouse_supply_id").autoIncrement()
    val warehouseId = integer("warehouse_id").references(WarehouseTable.warehouseId)
    val supplyId = long("supply_id").references(SupplyTable.supplyId)

    override val primaryKey = PrimaryKey(warehouseSupplyId)
}