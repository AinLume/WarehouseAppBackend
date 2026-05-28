package api.routes

import api.dto.CreateCategoryRequest
import api.dto.UpdateCategoryRequest
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
import service.CategoryService

fun Route.categoryRoutes() {

    val service by application.inject<CategoryService>()

    authenticate("jwt-auth") {
        route("/categories") {

            // GET /categories — список всех категорий
            get {
                val categories = service.getAllCategories()

                call.respond(HttpStatusCode.OK, categories.map { it.toResponse() })
            }

            // POST /categories — создать категорию
            post {
                val request = call.receive<CreateCategoryRequest>()
                val category = service.createCategory(request.title)

                call.respond(
                    HttpStatusCode.Created,
                    category.toResponse()
                )
            }

            route("/{id}") {

                // GET /categories/{id}
                get {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    val category = service.getCategoryById(id)

                    call.respond(
                        HttpStatusCode.OK,
                        category.toResponse()
                    )
                }

                // PUT /categories/{id}
                put {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    val request = call.receive<UpdateCategoryRequest>()
                    val category = service.updateCategoryById(id, request.title)

                    call.respond(
                        HttpStatusCode.OK,
                        category.toResponse()
                    )
                }

                // DELETE /categories/{id}
                delete {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    service.deleteCategoryById(id)

                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}