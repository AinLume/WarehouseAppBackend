package service

import domain.model.*
import domain.repository.*
import io.mockk.*

abstract class BaseServiceTest {
    protected val categoryRepository: CategoryRepository = mockk()
    protected val productRepository: ProductRepository = mockk()
    protected val supplierRepository: SupplierRepository = mockk()
    protected val warehouseRepository: WarehouseRepository = mockk()
    protected val warehouseProductRepository: WarehouseProductRepository = mockk()
    protected val supplyRepository: SupplyRepository = mockk()

    protected fun mockCategory(id: Int = 1, title: String = "Electronics") = Category(
        categoryId = id,
        title = title
    )

    protected fun mockProduct(
        id: Long = 1L,
        categoryId: Int = 1,
        title: String = "Laptop",
        description: String? = "Gaming laptop"
    ) = Product(
        productId = id,
        categoryId = categoryId,
        title = title,
        description = description,
        width = 30,
        length = 20,
        height = 5
    )

    protected fun mockProductEx(
        id: Long = 1L,
        categoryId: Int = 1,
        categoryTitle: String = "Electronics",
        title: String = "Laptop"
    ) = ProductEx(
        productId = id,
        categoryId = categoryId,
        categoryTitle = categoryTitle,
        title = title,
        description = "Gaming laptop",
        width = 30,
        length = 20,
        height = 5
    )

    protected fun mockSupplier(
        id: Int = 1,
        name: String = "TechSupplier",
        phone: String = "+79001234567",
        email: String? = "tech@supplier.com"
    ) = Supplier(
        supplierId = id,
        name = name,
        phone = phone,
        email = email,
        address = "Moscow"
    )

    protected fun mockWarehouse(
        id: Int = 1,
        title: String? = "Main Warehouse",
        address: String = "123 Main St"
    ) = Warehouse(
        warehouseId = id,
        title = title,
        address = address,
        capacity = 1000,
        width = 100L,
        length = 200L,
        height = 50L
    )

    protected fun mockSupply(
        id: Long = 1L,
        supplierId: Int = 1,
        status: SupplyStatus = SupplyStatus.CREATED,
        totalPrice: Long = 10000L
    ) = Supply(
        supplyId = id,
        supplierId = supplierId,
        status = status,
        totalPrice = totalPrice,
        createdAt = "2024-01-01T00:00:00",
        updatedAt = "2024-01-01T00:00:00"
    )

    protected fun mockSupplyEnriched(
        supply: Supply = mockSupply(),
        warehouseId: Int = 1,
        warehouseTitle: String? = "Main Warehouse",
        supplierName: String = "TechSupplier"
    ) = SupplyEnriched(
        supply = supply,
        warehouseId = warehouseId,
        warehouseTitle = warehouseTitle,
        supplierName = supplierName
    )

    protected fun mockSupplyProduct(
        id: Long = 1L,
        supplyId: Long = 1L,
        productId: Long = 1L,
        quantity: Int = 10,
        unitPrice: Long = 1000L
    ) = SupplyProduct(
        supplyProductId = id,
        supplyId = supplyId,
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice
    )

    protected fun mockWarehouseProductDetail(
        id: Long = 1L,
        quantity: Int = 10,
        product: Product = mockProduct()
    ) = WarehouseProductDetail(
        warehouseProductId = id,
        quantity = quantity,
        product = product
    )

    protected fun mockWarehouseStats(
        totalQuantity: Int = 100,
        totalPrice: Long = 50000L,
        uniqueProducts: Int = 10
    ) = WarehouseStats(
        totalQuantity = totalQuantity,
        totalPrice = totalPrice,
        uniqueProducts = uniqueProducts
    )
}
