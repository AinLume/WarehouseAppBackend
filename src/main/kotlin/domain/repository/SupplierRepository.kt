package domain.repository

import domain.model.Supplier

interface SupplierRepository {
    suspend fun findAll(): List<Supplier>
    suspend fun findById(id: Int): Supplier?
    suspend fun findAllByUserId(userId: Int): List<Supplier>
    suspend fun findByIdAndUserId(id: Int, userId: Int): Supplier?
    suspend fun create(name: String, phone: String, email: String?, address: String?, userId: Int): Supplier
    suspend fun update(id: Int, name: String?, phone: String?, email: String?, address: String?, userId: Int): Supplier?
    suspend fun delete(id: Int, userId: Int): Boolean
    suspend fun hasSupplies(id: Int): Boolean
    suspend fun hasAccess(supplierId: Int, userId: Int): Boolean
}