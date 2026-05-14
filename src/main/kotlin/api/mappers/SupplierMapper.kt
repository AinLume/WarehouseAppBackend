package api.mappers

import api.dto.CreateSupplierRequest
import api.dto.SupplierResponse
import domain.model.Supplier

fun Supplier.toResponse() = SupplierResponse(
    supplierId = supplierId,
    name = name,
    phone = phone,
    email = email,
    address = address
)

fun CreateSupplierRequest.toModel() = Supplier(0, name, phone, email, address)