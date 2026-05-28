package api.routes

import api.dto.CreateProductRequest
import api.dto.UpdateProductRequest
import api.mappers.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
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
import service.ProductService
import kotlin.getValue

fun Route.productRoutes() {

    val service by application.inject<ProductService>()

    authenticate("jwt-auth") {
        route("/products") {

            // GET /products?categoryId=1 — список, опциональный фильтр по категории
            get {
                val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()

                val products = service.getAllProducts(categoryId)

                call.respond(HttpStatusCode.OK, products.map { it.toResponse() })
            }

            // POST /products
            post {
                val request = call.receive<CreateProductRequest>()

                val product = service.createProduct(request)

                call.respond(HttpStatusCode.Created, product.toResponse())
            }

            route("/{id}") {

                // GET /products/{id}
                get {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )

                    val product = service.getProductById(id)

                    call.respond(HttpStatusCode.OK, product.toResponse())
                }

                // PUT /products/{id}
                put {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    val request = call.receive<UpdateProductRequest>()

                    val product = service.updateProductById(id, request)

                    call.respond(HttpStatusCode.OK, product.toResponse())
                }

                // DELETE /products/{id}
                delete {
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )

                    service.deleteProductById(id)

                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}