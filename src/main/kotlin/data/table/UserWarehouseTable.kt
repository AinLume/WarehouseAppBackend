package data.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserWarehouseTable : Table("user_warehouse") {
    val userId = integer("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val warehouseId = integer("warehouse_id").references(WarehouseTable.warehouseId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userId, warehouseId)
}
