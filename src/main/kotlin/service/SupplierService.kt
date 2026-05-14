package service

import api.dto.CreateSupplierRequest
import api.dto.UpdateSupplierRequest
import domain.model.Supplier
import domain.repository.SupplierRepository

class SupplierService(private val repository: SupplierRepository) {

    suspend fun getAllSuppliers(): List<Supplier> =
        repository.findAll()

    suspend fun getSupplierById(id: Int): Supplier =
        repository.findById(id)
            ?: throw NoSuchElementException("Supplier $id not found")

    suspend fun createSupplier(dto: CreateSupplierRequest): Supplier {
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
            dto.address?.trim()
        )
    }

    suspend fun updateSupplierById(id: Int, dto: UpdateSupplierRequest): Supplier {
        getSupplierById(id)

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
            dto.address?.trim()
        ) ?: throw NoSuchElementException("Supplier $id not found")
    }

    suspend fun deleteSupplierById(id: Int) {
        if (repository.hasSupplies(id))
            throw IllegalArgumentException("Cannot delete supplier with existing supplies")

        val deleted = repository.delete(id)
        if (!deleted) throw NoSuchElementException("Supplier $id not found")
    }
}