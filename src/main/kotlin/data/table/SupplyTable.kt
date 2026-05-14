package data.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object SupplyTable : Table("supply") {
    val supplyId = long("supply_id").autoIncrement()
    val supplierId = integer("supplier_id").references(SupplierTable.supplierId)
    val status = varchar("status", 20)
    val totalPrice = long("total_price").default(0)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(supplyId)
}