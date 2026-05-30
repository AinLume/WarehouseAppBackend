package data.repository

import data.table.CategoryTable
import data.table.ProductTable
import data.table.UserCategoryTable
import domain.model.Category
import domain.repository.CategoryRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import utils.dbQuery

class CategoryRepositoryImpl : CategoryRepository {

    private fun rowToCategory(row: ResultRow) = Category(
        categoryId = row[CategoryTable.categoryId],
        title = row[CategoryTable.title]
    )

    override suspend fun findAll(): List<Category> = dbQuery {
        CategoryTable.selectAll().map(::rowToCategory)
    }

    override suspend fun findById(id: Int): Category? = dbQuery {
        CategoryTable
            .selectAll()
            .where { CategoryTable.categoryId eq id }
            .map(::rowToCategory)
            .singleOrNull()
    }

    override suspend fun findAllByUserId(userId: Int): List<Category> = dbQuery {
        (CategoryTable innerJoin UserCategoryTable)
            .selectAll()
            .where { UserCategoryTable.userId eq userId }
            .map(::rowToCategory)
    }

    override suspend fun findByIdAndUserId(id: Int, userId: Int): Category? = dbQuery {
        (CategoryTable innerJoin UserCategoryTable)
            .selectAll()
            .where {
                (CategoryTable.categoryId eq id) and
                (UserCategoryTable.userId eq userId)
            }
            .map(::rowToCategory)
            .singleOrNull()
    }

    override suspend fun create(title: String, userId: Int): Category = dbQuery {
        val id = CategoryTable.insert {
            it[CategoryTable.title] = title
        } get CategoryTable.categoryId

        UserCategoryTable.insert {
            it[UserCategoryTable.userId] = userId
            it[UserCategoryTable.categoryId] = id
        }

        Category(id, title)
    }

    override suspend fun update(id: Int, title: String, userId: Int): Category? = dbQuery {
        if (!hasAccess(id, userId)) return@dbQuery null

        val updated = CategoryTable.update(
            where = { CategoryTable.categoryId eq id }
        ) {
            it[CategoryTable.title] = title
        }
        if (updated == 0) null else findById(id)
    }

    override suspend fun delete(id: Int, userId: Int): Boolean = dbQuery {
        val hasAccess = hasAccess(id, userId)
        if (!hasAccess) return@dbQuery false

        UserCategoryTable.deleteWhere {
            (UserCategoryTable.categoryId eq id) and
            (UserCategoryTable.userId eq userId)
        }

        CategoryTable.deleteWhere { categoryId eq id } > 0
    }

    override suspend fun existsByTitle(title: String): Boolean = dbQuery {
        CategoryTable
            .selectAll()
            .where { CategoryTable.title eq title }
            .count() > 0
    }

    override suspend fun hasProducts(id: Int): Boolean = dbQuery {
        ProductTable
            .selectAll()
            .where { ProductTable.categoryId eq id }
            .count() > 0
    }

    override suspend fun hasAccess(categoryId: Int, userId: Int): Boolean = dbQuery {
        UserCategoryTable
            .selectAll()
            .where {
                (UserCategoryTable.categoryId eq categoryId) and
                (UserCategoryTable.userId eq userId)
            }
            .count() > 0
    }
}
