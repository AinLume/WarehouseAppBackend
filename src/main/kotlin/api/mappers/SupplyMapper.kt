package api.mappers

import api.dto.SupplyDetailResponse
import api.dto.SupplyProductDetailResponse
import api.dto.SupplyProductResponse
import api.dto.SupplyResponse
import domain.model.Supply
import domain.model.SupplyProduct
import domain.model.SupplyProductDetail

fun Supply.toResponse(warehouseId: Int, warehouseTitle: String?, supplierName: String) = SupplyResponse(
    supplyId = supplyId,
    supplierId = supplierId,
    supplierName = supplierName,
    warehouseId = warehouseId,
    warehouseTitle = warehouseTitle,
    status = status.name,
    totalPrice = totalPrice,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SupplyProduct.toResponse() = SupplyProductResponse(
    supplyId = supplyId,
    productId = productId,
    quantity = quantity,
    unitPrice = unitPrice,
    totalPrice = quantity * unitPrice
)

fun Supply.toDetailResponse(
    warehouseId: Int,
    warehouseTitle: String?,
    supplierName: String,
    products: List<SupplyProductDetail>
) = SupplyDetailResponse(
    supplyId = supplyId,
    supplierId = supplierId,
    supplierName = supplierName,
    warehouseId = warehouseId,
    warehouseTitle = warehouseTitle,
    status = status.name,
    totalPrice = totalPrice,
    createdAt = createdAt,
    updatedAt = updatedAt,
    products = products.map { it.toResponse() }
)

fun SupplyProductDetail.toResponse() = SupplyProductDetailResponse(
    supplyProductId = supplyProductId,
    quantity = quantity,
    unitPrice = unitPrice,
    product = product.toResponse()
)