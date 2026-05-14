package data.table

import org.jetbrains.exposed.sql.Table

object ProductTable : Table("product") {
    val productId = long("product_id").autoIncrement()
    val categoryId = integer("category_id").references(CategoryTable.categoryId)
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val width = integer("width")
    val length = integer("length")
    val height = integer("height")

    override val primaryKey = PrimaryKey(productId)
}