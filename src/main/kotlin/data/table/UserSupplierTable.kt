package data.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserSupplierTable : Table("user_supplier") {
    val userId = integer("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val supplierId = integer("supplier_id").references(SupplierTable.supplierId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userId, supplierId)
}
