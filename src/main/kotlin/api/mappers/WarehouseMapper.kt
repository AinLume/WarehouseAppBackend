package api.mappers

import api.dto.WarehouseProductDetailResponse
import api.dto.WarehouseResponse
import domain.model.Warehouse
import domain.model.WarehouseProductDetail

fun Warehouse.toResponse() = WarehouseResponse(
    warehouseId = warehouseId,
    title = title,
    address = address,
    capacity = capacity,
    width = width,
    length = length,
    height = height
)


fun WarehouseProductDetail.toResponse() = WarehouseProductDetailResponse(
    warehouseProductId = warehouseProductId,
    quantity = quantity,
    product = product.toResponse()
)