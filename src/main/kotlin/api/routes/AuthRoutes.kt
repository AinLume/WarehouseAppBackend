package api.routes

import api.dto.LoginRequest
import api.dto.RefreshTokenRequest
import api.dto.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import plugins.UserIdPrincipal
import service.AuthService

fun Route.authRoutes() {

    val authService by application.inject<AuthService>()

    route("/auth") {

        // POST /auth/register — регистрация (открытый эндпоинт)
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)

            call.respond(HttpStatusCode.Created, response)
        }

        // POST /auth/login — вход (открытый эндпоинт)
        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)

            call.respond(HttpStatusCode.OK, response)
        }

        // POST /auth/refresh — обновление токена (открытый эндпоинт)
        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val response = authService.refreshToken(request.refreshToken)

            call.respond(HttpStatusCode.OK, response)
        }

        // GET /auth/me — информация о текущем пользователе (защищённый)
        authenticate("jwt-auth") {
            get("/me") {
                val principal = call.principal<UserIdPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))

                val user = authService.me(principal.userId)
                call.respond(HttpStatusCode.OK, user)
            }
        }
    }
}
