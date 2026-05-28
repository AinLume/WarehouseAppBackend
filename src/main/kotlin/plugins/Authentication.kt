package plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.jwt
import service.JwtService

data class UserIdPrincipal(val userId: Int, val username: String, val role: String)

fun Application.configureAuthentication(jwtService: JwtService) {
    install(Authentication) {
        jwt("jwt-auth") {
            val jwtVerifier = JWT
                .require(Algorithm.HMAC256(jwtService.secret))
                .withAudience(jwtService.audience)
                .withIssuer(jwtService.issuer)
                .build()

            verifier(jwtVerifier)

            realm = jwtService.issuer

            validate { credential: JWTCredential ->
                val tokenType = credential.payload.getClaim("type").asString()
                if (tokenType != "access") {
                    return@validate null
                }

                val userId = credential.payload.getClaim("userId").asInt()
                val username = credential.payload.getClaim("username").asString()
                val role = credential.payload.getClaim("role").asString()

                UserIdPrincipal(userId, username, role)
            }
        }
    }
}
