package plugins

import api.routes.authRoutes
import api.routes.categoryRoutes
import api.routes.productRoutes
import api.routes.supplierRoutes
import api.routes.supplyRoutes
import api.routes.warehouseRoutes
import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/docs-v1.yml")
        route("/api/v1") {
            authRoutes()
            categoryRoutes()
            productRoutes()
            supplierRoutes()
            warehouseRoutes()
            supplyRoutes()

            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }
        }
    }
}