package data.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserProductTable : Table("user_product") {
    val userId = integer("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = long("product_id").references(ProductTable.productId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userId, productId)
}
