package api.routes

import api.dto.AddSupplyProductRequest
import api.dto.CreateSupplyRequest
import api.dto.UpdateSupplyStatusRequest
import api.mappers.toDetailResponse
import api.mappers.toResponse
import domain.model.SupplyStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import service.SupplyService
import utils.enumFromStringOrThrow

fun Route.supplyRoutes() {

    val service by application.inject<SupplyService>()

    authenticate("jwt-auth") {
        route("/supplies") {

            // GET /supplies?warehouseId=1&status=CREATED
            get {
                val warehouseId = call.request.queryParameters["warehouseId"]?.toIntOrNull()
                val status = enumFromStringOrThrow<SupplyStatus>(
                    call.request.queryParameters["status"]
                )

                val supplies = service.getAllSupplies(warehouseId, status)
                call.respond(HttpStatusCode.OK,
                    supplies.map { it.supply.toResponse(
                        it.warehouseId, it.warehouseTitle, it.supplierName
                    ) })
            }

            // POST /supplies — создать поставку
            post {
                val request = call.receive<CreateSupplyRequest>()
                val supply = service.createSupply(request)

                call.respond(HttpStatusCode.Created,
                    supply.supply.toResponse(
                        supply.warehouseId,
                        supply.warehouseTitle,
                        supply.supplierName
                    ))
            }

            route("/{id}") {

                // GET /supplies/{id} — детальная инфа + список товаров
                get {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id"))

                    val detail = service.getSupplyDetails(id)
                    call.respond(HttpStatusCode.OK, detail)
                }

                // PATCH /supplies/{id}/status — изменить статус
                patch("/status") {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@patch call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )

                    val request = call.receive<UpdateSupplyStatusRequest>()
                    if (request.status.isBlank())
                        return@patch call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Status is required")
                        )

                    val newStatus = enumFromStringOrThrow<SupplyStatus>(request.status)!!
                    val supply = service.updateSupplyStatus(id, newStatus)

                    call.respond(HttpStatusCode.OK,
                        supply.supply.toResponse(
                            supply.warehouseId,
                            supply.warehouseTitle,
                            supply.supplierName
                        )
                    )
                }

                // DELETE /supplies/{id} — удалить (только CREATED или CANCELLED)
                delete {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )

                    service.deleteSupplyById(id)

                    call.respond(HttpStatusCode.NoContent)
                }

                // POST /supplies/{id}/products — добавить товар в поставку
                post("/products") {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                    val request = call.receive<AddSupplyProductRequest>()

                    val product = service.addProductToSupply(id, request)

                    call.respond(HttpStatusCode.Created, product.toResponse())
                }

                // DELETE /supplies/{id}/products/{productId} — убрать товар из поставки
                delete("/products/{productId}") {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    val productId = call.parameters["productId"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid productId")
                        )

                    service.removeProduct(id, productId)

                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}