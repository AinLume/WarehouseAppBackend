package service

import api.dto.CreateSupplierRequest
import api.dto.UpdateSupplierRequest
import domain.model.Supplier
import domain.repository.SupplierRepository

class SupplierService(private val repository: SupplierRepository) {

    suspend fun getAllSuppliers(userId: Int): List<Supplier> =
        repository.findAllByUserId(userId)

    suspend fun getSupplierById(id: Int, userId: Int): Supplier =
        repository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("Supplier $id not found")

    suspend fun createSupplier(dto: CreateSupplierRequest, userId: Int): Supplier {
        if (dto.name.isBlank())
            throw IllegalArgumentException("Name must not be blank")

        if (!dto.phone.matches(Regex("^(\\+7|8)\\d{10}\$")))
            throw IllegalArgumentException("Invalid phone format")

        dto.email?.let {
            if (!it.matches(Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}\$")))
                throw IllegalArgumentException("Invalid email format")
        }

        return repository.create(
            dto.name.trim(),
            dto.phone,
            dto.email,
            dto.address?.trim(),
            userId
        )
    }

    suspend fun updateSupplierById(id: Int, dto: UpdateSupplierRequest, userId: Int): Supplier {
        getSupplierById(id, userId)

        if (dto.name != null && dto.name.isBlank())
            throw IllegalArgumentException("Name must not be blank")
        dto.phone?.let {
            if (!it.matches(Regex("^(\\+7|8)\\d{10}\$")))
                throw IllegalArgumentException("Invalid phone format")
        }
        dto.email?.let {
            if (!it.matches(Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}\$")))
                throw IllegalArgumentException("Invalid email format")
        }

        return repository.update(
            id,
            dto.name?.trim(),
            dto.phone,
            dto.email,
            dto.address?.trim(),
            userId
        ) ?: throw NoSuchElementException("Supplier $id not found")
    }

    suspend fun deleteSupplierById(id: Int, userId: Int) {
        getSupplierById(id, userId)

        if (repository.hasSupplies(id))
            throw IllegalArgumentException("Cannot delete supplier with existing supplies")

        val deleted = repository.delete(id, userId)
        if (!deleted) throw NoSuchElementException("Supplier $id not found")
    }
}