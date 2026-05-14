package data.table

import org.jetbrains.exposed.sql.Table

object SupplyProductTable : Table("supply_product") {
    val supplyProductId = long("supply_product_id").autoIncrement()
    val supplyId        = long("supply_id").references(SupplyTable.supplyId)
    val productId       = long("product_id").references(ProductTable.productId)
    val quantity        = integer("quantity")
    val unitPrice       = long("unit_price")

    override val primaryKey = PrimaryKey(supplyProductId)
}