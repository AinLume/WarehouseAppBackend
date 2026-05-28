package plugins

import data.repository.*
import domain.repository.*
import data.repository.UserRepositoryImpl
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import service.*
import utils.SecretLoader

val appModule = module(createdAtStart = true) {

    single<CategoryRepository> { CategoryRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<SupplierRepository> { SupplierRepositoryImpl() }
    single<WarehouseRepository> { WarehouseRepositoryImpl() }
    single<SupplyRepository> { SupplyRepositoryImpl() }
    single<WarehouseProductRepository> { WarehouseProductRepositoryImpl() }
    single { UserRepositoryImpl() }

    single { CategoryService(get()) }
    single { ProductService(get()) }
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
    val config = environment.config

    val jwtModule = module {
        single {
            val privateKey = try {
                val keyPath = System.getenv("PRIVATE_KEY_PATH") ?: config.property("jwt.privateKeyPath").getString()
                SecretLoader.load(keyPath)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to load private key", e)
            }

            val publicKey = try {
                val keyPath = System.getenv("PUBLIC_KEY_PATH") ?: config.property("jwt.publicKeyPath").getString()
                SecretLoader.load(keyPath)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to load public key", e)
            }

            JwtService(
                privateKeyContent = privateKey,
                publicKeyContent = publicKey,
                issuer = "warehouse-app",
                audience = "warehouse-api"
            )
        }
        single { AuthService(get(), get()) }
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule, jwtModule)
    }

    configureAuthentication(get())
}