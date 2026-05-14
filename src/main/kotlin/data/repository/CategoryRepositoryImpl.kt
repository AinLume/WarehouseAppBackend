package data.repository

import data.table.CategoryTable
import data.table.ProductTable
import domain.model.Category
import domain.repository.CategoryRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    override suspend fun create(title: String): Category = dbQuery {
        val id = CategoryTable.insert {
            it[CategoryTable.title] = title
        } get CategoryTable.categoryId

        Category(id, title)
    }

    override suspend fun update(id: Int, title: String): Category? = dbQuery {
        val updated = CategoryTable.update(
            where = { CategoryTable.categoryId eq id }
        ) {
            it[CategoryTable.title] = title
        }
        if (updated == 0) null else findById(id)
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
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
}