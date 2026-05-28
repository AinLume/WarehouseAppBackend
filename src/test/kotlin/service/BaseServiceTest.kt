package service

import api.dto.AddSupplyProductRequest
import api.dto.CreateProductRequest
import api.dto.CreateSupplierRequest
import api.dto.CreateSupplyRequest
import api.dto.CreateWarehouseRequest
import api.dto.UpdateProductRequest
import api.dto.UpdateSupplierRequest
import api.dto.UpdateWarehouseRequest
import domain.model.Category
import domain.model.Product
import domain.model.Supplier
import domain.model.Supply
import domain.model.SupplyEnriched
import domain.model.SupplyProduct
import domain.model.SupplyProductDetail
import domain.model.SupplyStatus
import domain.model.Warehouse
import domain.model.WarehouseProductDetail
import domain.repository.CategoryRepository
import domain.repository.ProductRepository
import domain.repository.SupplierRepository
import domain.repository.SupplyRepository
import domain.repository.WarehouseProductRepository
import domain.repository.WarehouseRepository
import io.mockk.clearAllMocks
import io.mockk.mockk
import kotlin.test.AfterTest

abstract class BaseServiceTest {
    val categoryRepository = mockk<CategoryRepository>()
    val productRepository = mockk<ProductRepository>()
    val supplierRepository = mockk<SupplierRepository>()
    val warehouseRepository = mockk<WarehouseRepository>()
    val supplyRepository = mockk<SupplyRepository>()
    val warehouseProductRepository = mockk<WarehouseProductRepository>()

    val categoryService = CategoryService(categoryRepository)
    val supplierService = SupplierService(supplierRepository)
    val warehouseService = WarehouseService(warehouseRepository, warehouseProductRepository)
    val productService = ProductService(productRepository, categoryService)
    val supplyService = SupplyService(
        supplyRepository,
        supplierService,
        warehouseService,
        productService,
        warehouseProductRepository
    )

    // Category
    val testCategory = Category(
        categoryId = 1,
        title = "Electronics"
    )
    val testCategories = listOf(
        testCategory,
        Category(categoryId = 2, title = "Food")
    )

    // Product
    val testProduct = Product(
        productId = 1L,
        categoryId = 1,
        title = "Laptop",
        description = "Gaming laptop"
    )
    val testProducts = listOf(
        testProduct,
        testProduct.copy(productId = 2L, title = "Phone", description = null)
    )
    val createProductRequest = CreateProductRequest(
        categoryId = 1,
        title = "Laptop",
        description = "Gaming laptop"
    )
    val updateProductRequest = UpdateProductRequest(
        categoryId = null,
        title = "New Laptop",
        description = null
    )

    // Supplier
    val testSupplier = Supplier(
        supplierId = 1,
        name = "ООО Поставщик",
        phone = "+79991234567",
        email = "supplier@example.com",
        address = "Москва, ул. Ленина, 1"
    )
    val testSuppliers = listOf(
        testSupplier,
        testSupplier.copy(supplierId = 2, name = "ИП Иванов", email = null)
    )
    val createSupplierRequest = CreateSupplierRequest(
        name = "ООО Поставщик",
        phone = "+79991234567",
        email = "supplier@example.com",
        address = "Москва, ул. Ленина, 1"
    )

    val updateSupplierRequest = UpdateSupplierRequest(
        name = "Новый поставщик",
        phone = null,
        email = null,
        address = null
    )

    // Warehouse
    val testWarehouse = Warehouse(
        warehouseId = 1,
        title = "Главный склад",
        address = "Москва, ул. Складская, 5"
    )

    val createWarehouseRequest = CreateWarehouseRequest(
        title = "Главный склад",
        address = "Москва, ул. Складская, 5"
    )

    val updateWarehouseRequest = UpdateWarehouseRequest(
        title = "Новое название",
        address = null
    )

    // WarehouseProduct

    val testProductDetail = WarehouseProductDetail(
        warehouseProductId = 1L,
        quantity = 20,
        product = Product(
            productId = 1L,
            categoryId = 1,
            title = "Тестовый продукт",
            description = "Описание тестового продукта"
        )
    )

    // Supply
    val testSupply = Supply(
        supplyId  = 1L,
        supplierId = 1,
        status = SupplyStatus.CREATED,
        totalPrice = 0L,
        createdAt = "2026-03-11T10:00:00Z",
        updatedAt = "2026-03-11T10:00:00Z"
    )
    val testSupplyEnriched = SupplyEnriched(
        supply = testSupply,
        warehouseId = 1,
        warehouseTitle = "Main Warehouse",
        supplierName = "Test Supplier"
    )
    val testSupplies = listOf(
        testSupplyEnriched,
        testSupplyEnriched.copy(supply = testSupply.copy(supplyId = 2L, status = SupplyStatus.PENDING))
    )
    val testSupplyProduct = SupplyProduct(
        supplyProductId = 1L,
        supplyId = 1L,
        productId = 1L,
        quantity = 10,
        unitPrice = 5000L
    )
    val createSupplyRequest = CreateSupplyRequest(
        supplierId = 1,
        warehouseId = 1
    )

    val addSupplyProductRequest = AddSupplyProductRequest(
        productId = 1L,
        quantity = 10,
        unitPrice = 5000L
    )

    val testSupplyProductDetail = SupplyProductDetail(
        supplyProductId = 1L,
        quantity = 10,
        unitPrice = 5000L,
        product = testProduct
    )

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }
}
