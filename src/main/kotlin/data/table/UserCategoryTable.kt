package data.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UserCategoryTable : Table("user_category") {
    val userId = integer("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val categoryId = integer("category_id").references(CategoryTable.categoryId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userId, categoryId)
}
