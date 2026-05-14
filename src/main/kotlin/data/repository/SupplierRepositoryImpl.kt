package data.repository

import data.table.SupplierTable
import data.table.SupplyTable
import domain.model.Supplier
import domain.repository.SupplierRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import utils.dbQuery

class SupplierRepositoryImpl : SupplierRepository {

    private fun rowToSupplier(row: ResultRow) = Supplier(
        supplierId = row[SupplierTable.supplierId],
        name = row[SupplierTable.name],
        phone = row[SupplierTable.phone],
        email = row[SupplierTable.email],
        address = row[SupplierTable.address]
    )

    override suspend fun findAll(): List<Supplier> = dbQuery {
        SupplierTable.selectAll().map(::rowToSupplier)
    }

    override suspend fun findById(id: Int): Supplier? = dbQuery {
        SupplierTable
            .selectAll()
            .where { SupplierTable.supplierId eq id }
            .map(::rowToSupplier)
            .singleOrNull()
    }

    override suspend fun create(
        name: String,
        phone: String,
        email: String?,
        address: String?
    ): Supplier = dbQuery {

        val id = SupplierTable.insert {
            it[SupplierTable.name] = name
            it[SupplierTable.phone] = phone
            it[SupplierTable.email] = email
            it[SupplierTable.address] = address
        } get SupplierTable.supplierId

        Supplier(id, name, phone, email, address)
    }

    override suspend fun update(
        id: Int,
        name: String?,
        phone: String?,
        email: String?,
        address: String?
    ): Supplier? = dbQuery {

        val updated = SupplierTable.update(
            where = { SupplierTable.supplierId eq id }
        ) {
            name?.let {v -> it[SupplierTable.name] = v}
            phone?.let {v -> it[SupplierTable.phone] = v}
            email?.let {v -> it[SupplierTable.email] = v}
            address?.let {v -> it[SupplierTable.address] = v}
        }

        if (updated == 0) null else findById(id)
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        SupplierTable.deleteWhere { SupplierTable.supplierId eq id } != 0
    }

    override suspend fun hasSupplies(id: Int): Boolean = dbQuery {
        SupplyTable
            .selectAll()
            .where { SupplyTable.supplierId eq id }
            .count() > 0
    }
}