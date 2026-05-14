package plugins

import data.repository.*
import domain.repository.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import service.*

val appModule = module {

    single<CategoryRepository> { CategoryRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<SupplierRepository> { SupplierRepositoryImpl() }
    single<WarehouseRepository> { WarehouseRepositoryImpl() }
    single<SupplyRepository> { SupplyRepositoryImpl() }
    single<WarehouseProductRepository> { WarehouseProductRepositoryImpl() }

    single { CategoryService(get()) }
    single { ProductService(get(), get()) }
    single { SupplierService(get()) }
    single { WarehouseService(get(), get()) }
    single { SupplyService(
        get(),
        get(),
        get(),
        get(),
        get()
    ) }
    single { StatsService(
        get(),
        get(),
        get()
    ) }
}

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}