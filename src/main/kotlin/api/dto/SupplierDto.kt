package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSupplierRequest(
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class UpdateSupplierRequest(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class SupplierResponse(
    val supplierId: Int,
    val name: String,
    val phone: String,
    val email: String?,
    val address: String?
)