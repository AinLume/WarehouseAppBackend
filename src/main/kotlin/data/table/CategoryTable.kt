package data.table

import org.jetbrains.exposed.sql.Table

object CategoryTable : Table("category") {
    val categoryId = integer("category_id").autoIncrement()
    val title = varchar("title", 100)

    override val primaryKey = PrimaryKey(categoryId)
}