package domain.repository

import domain.model.Supplier

interface SupplierRepository {
    suspend fun findAll(): List<Supplier>
    suspend fun findById(id: Int): Supplier?
    suspend fun create(name: String, phone: String, email: String?, address: String?): Supplier
    suspend fun update(id: Int, name: String?, phone: String?, email: String?, address: String?): Supplier?
    suspend fun delete(id: Int): Boolean
    suspend fun hasSupplies(id: Int): Boolean
}