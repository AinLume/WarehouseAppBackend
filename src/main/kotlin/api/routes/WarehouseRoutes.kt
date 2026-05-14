package api.routes

import api.dto.CreateWarehouseRequest
import api.dto.UpdateWarehouseRequest
import api.dto.WarehouseCategoryGroupResponse
import api.mappers.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import service.StatsService
import service.WarehouseService
import kotlin.collections.map

fun Route.warehouseRoutes() {

    val service by application.inject<WarehouseService>()
    val statsService by application.inject<StatsService>()

    val log = LoggerFactory.getLogger("StatsRoute")

    route("/warehouses") {

        // GET /warehouses
        get {
            val warehouses = service.getAllWarehouses()

            call.respond(HttpStatusCode.OK, warehouses.map { it.toResponse() })
        }

        // POST /warehouses
        post {
            val request = call.receive<CreateWarehouseRequest>()
            val warehouse = service.createWarehouse(request)

            call.respond(HttpStatusCode.Created, warehouse.toResponse())
        }

        // GET /warehouses/stats — статистика глобальная по складам
        get("/stats") {
            val stats = statsService.getGlobalStats()

            log.info("Stats: $stats")

            call.respond(HttpStatusCode.OK, stats)
        }

        route("/{id}") {

            // GET /warehouses/{id}
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid id")
                    )
                val warehouse = service.getWarehouseById(id)

                call.respond(HttpStatusCode.OK, warehouse.toResponse())
            }

            // PUT /warehouses/{id}
            put {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid id")
                    )
                val request = call.receive<UpdateWarehouseRequest>()

                val warehouse = service.updateWarehouseById(id, request)

                call.respond(HttpStatusCode.OK, warehouse.toResponse())
            }

            // DELETE /warehouses/{id}
            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid id")
                    )

                service.deleteWarehouseById(id)

                call.respond(HttpStatusCode.NoContent)
            }

            // GET /warehouses/{id}/products — товары на складе сгруппированные по категориям
            get("/products") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))

                val grouped = service.getProductsGroupedByCategory(id)

                call.respond(HttpStatusCode.OK,
                    grouped.map { (category, products) ->
                        WarehouseCategoryGroupResponse(
                            categoryId    = category.categoryId,
                            categoryTitle = category.title,
                            products      = products.map { it.toResponse() }
                        )
                    }
                )
            }
        }
    }
}