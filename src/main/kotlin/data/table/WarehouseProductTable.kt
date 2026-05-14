package data.table

import org.jetbrains.exposed.sql.Table

object WarehouseProductTable : Table("warehouse_product") {
    val warehouseProductId = long("warehouse_product_id").autoIncrement()
    val warehouseId        = integer("warehouse_id").references(WarehouseTable.warehouseId)
    val productId          = long("product_id").references(ProductTable.productId)
    val quantity           = integer("quantity")
    val totalPrice         = long("total_price")

    override val primaryKey = PrimaryKey(warehouseProductId)
}