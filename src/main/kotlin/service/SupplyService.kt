package service

import api.dto.AddSupplyProductRequest
import api.dto.CreateSupplyRequest
import api.dto.SupplyDetailResponse
import api.mappers.toDetailResponse
import domain.model.Supply
import domain.model.SupplyEnriched
import domain.model.SupplyProduct
import domain.model.SupplyProductDetail
import domain.model.SupplyStatus
import domain.repository.SupplyRepository
import domain.repository.WarehouseProductRepository
import org.slf4j.LoggerFactory

class SupplyService(
    private val repository: SupplyRepository,
    private val supplierService: SupplierService,
    private val warehouseService: WarehouseService,
    private val productService: ProductService,
    private val warehouseProductRepository: WarehouseProductRepository
) {

    private val log = LoggerFactory.getLogger("SupplyService")

    suspend fun getAllSupplies(warehouseId: Int?, status: SupplyStatus?): List<SupplyEnriched> =
        repository.findAll(warehouseId, status)

    suspend fun getSupplyById(id: Long): SupplyEnriched =
        repository.findById(id)
            ?: throw NoSuchElementException("Supply $id not found")

    suspend fun getSupplyDetails(supplyId: Long): SupplyDetailResponse {
        val enriched = getSupplyById(supplyId)
        val products = repository.findProductsDetails(supplyId)

        return enriched.supply.toDetailResponse(
            enriched.warehouseId,
            enriched.warehouseTitle,
            enriched.supplierName,
            products
        )
    }

    suspend fun createSupply(dto: CreateSupplyRequest): SupplyEnriched {
        supplierService.getSupplierById(dto.supplierId)
        warehouseService.getWarehouseByIdInternal(dto.warehouseId)

        return repository.create(dto.supplierId, dto.warehouseId)
    }

    suspend fun updateSupplyStatus(id: Long, newStatus: SupplyStatus): SupplyEnriched {
        val enriched = getSupplyById(id)

        if (newStatus !in enriched.supply.status.allowedTransitions())
            throw IllegalArgumentException("Cannot transition from ${enriched.supply.status} to $newStatus")

        if (newStatus == SupplyStatus.COMPLETED) {
            completeSupply(id)
        }

        return repository.updateStatus(id, newStatus)
            ?: throw NoSuchElementException("Supply $id not found")
    }

    suspend fun addProductToSupply(supplyId: Long, dto: AddSupplyProductRequest): SupplyProduct {
        val enriched = getSupplyById(supplyId)

        if (enriched.supply.status != SupplyStatus.CREATED)
            throw IllegalArgumentException("Can only add products to supply with status CREATED")

        productService.getProductById(dto.productId)

        if (dto.quantity <= 0)
            throw IllegalArgumentException("Quantity must be positive")
        if (dto.unitPrice <= 0)
            throw IllegalArgumentException("Unit price must be positive")

        val product = repository.addProduct(supplyId, dto.productId, dto.quantity, dto.unitPrice)
        repository.updateTotalPrice(supplyId)

        return product
    }

    suspend fun removeProduct(supplyId: Long, productId: Long) {
        val enriched = getSupplyById(supplyId)

        if (enriched.supply.status != SupplyStatus.CREATED)
            throw IllegalArgumentException("Can only remove products from supply with status CREATED")

        val removed = repository.removeProduct(supplyId, productId)
        if (!removed) throw NoSuchElementException("Product $productId not found in supply $supplyId")

        repository.updateTotalPrice(supplyId)
    }

    suspend fun deleteSupplyById(id: Long) {
        val enriched = getSupplyById(id)

        if (enriched.supply.status !in listOf(SupplyStatus.CREATED, SupplyStatus.CANCELLED))
            throw IllegalArgumentException("Can only delete supply with status CREATED or CANCELLED")

        repository.delete(id)
    }

    private suspend fun completeSupply(supplyId: Long) {
        val warehouseId = warehouseService.getWarehouseIdBySupplyIdInternal(supplyId)

        log.info("Complete supply: wsId: $warehouseId, sId: $supplyId")

        val products = repository.findProducts(supplyId)

        log.info("Products: $products")

        warehouseProductRepository.upsertAll(warehouseId, products)
    }
}