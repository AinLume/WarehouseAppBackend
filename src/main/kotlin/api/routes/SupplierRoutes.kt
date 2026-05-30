package api.routes

import api.dto.CreateSupplierRequest
import api.dto.UpdateSupplierRequest
import api.mappers.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
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
import plugins.UserIdPrincipal
import service.SupplierService

fun Route.supplierRoutes() {

    val service by application.inject<SupplierService>()

    authenticate("jwt-auth") {
        route("/suppliers") {

            // GET /suppliers
            get {
                val principal = call.principal<UserIdPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                val suppliers = service.getAllSuppliers(principal.userId)

                call.respond(HttpStatusCode.OK, suppliers.map { it.toResponse() })
            }

            // POST /suppliers
            post {
                val principal = call.principal<UserIdPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                val request = call.receive<CreateSupplierRequest>()
                val supplier = service.createSupplier(request, principal.userId)

                call.respond(HttpStatusCode.Created, supplier.toResponse())
            }

            route("/{id}") {

                // GET /suppliers/{id}
                get {
                    val principal = call.principal<UserIdPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))

                    val supplier = service.getSupplierById(id, principal.userId)

                    call.respond(HttpStatusCode.OK, supplier.toResponse())
                }

                // PUT /suppliers/{id}
                put {
                    val principal = call.principal<UserIdPrincipal>()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))

                    val request = call.receive<UpdateSupplierRequest>()
                    val supplier = service.updateSupplierById(id, request, principal.userId)

                    call.respond(HttpStatusCode.OK, supplier.toResponse())
                }

                // DELETE /suppliers/{id}
                delete {
                    val principal = call.principal<UserIdPrincipal>()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))

                    service.deleteSupplierById(id, principal.userId)

                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}