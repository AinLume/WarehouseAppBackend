package plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val config = environment.config

    val host = System.getenv("DB_HOST") ?: config.property("postgres.host").getString()
    val port = System.getenv("DB_PORT") ?: config.property("postgres.port").getString()
    val name = System.getenv("DB_NAME") ?: config.property("postgres.name").getString()
    val user = System.getenv("DB_USER") ?: config.property("postgres.user").getString()
    val password = System.getenv("DB_PASSWORD") ?: config.property("postgres.password").getString()

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://$host:$port/$name"
        driverClassName = "org.postgresql.Driver"
        username = user
        this.password = password
        maximumPoolSize = 10
    }

    val dataSource = HikariDataSource(hikariConfig)

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .sqlMigrationPrefix("V")
        .sqlMigrationSeparator("__")
        .validateMigrationNaming(true)
        .load()
        .migrate()

    Database.connect(dataSource)
}