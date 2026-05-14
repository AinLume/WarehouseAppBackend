package data.table

import org.jetbrains.exposed.sql.Table

object SupplierTable : Table("supplier") {
    val supplierId = integer("supplier_id").autoIncrement()
    val name = varchar("name", 100)
    val phone = varchar("phone", 12)
    val email = varchar("email", 100).nullable()
    val address = text("address").nullable()

    override val primaryKey = PrimaryKey(supplierId)
}