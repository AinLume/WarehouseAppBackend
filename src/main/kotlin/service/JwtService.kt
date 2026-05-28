package service
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtService(
    val secret: String,
    val issuer: String,
    val audience: String,
    private val accessTokenLifetimeMs: Long = 1000 * 60 * 15,
    private val refreshTokenLifetimeMs: Long = 1000 * 60 * 60 * 24 * 7
) {
    fun generateAccessToken(userId: Int, username: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", role)
            .withClaim("type", "access")
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenLifetimeMs))
            .sign(Algorithm.HMAC256(secret))
    }

    fun generateRefreshToken(userId: Int): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenLifetimeMs))
            .sign(Algorithm.HMAC256(secret))
    }

    fun generateTokenPair(userId: Int, username: String, role: String): TokenPair {
        return TokenPair(
            accessToken = generateAccessToken(userId, username, role),
            refreshToken = generateRefreshToken(userId)
        )
    }
}

data class TokenPair(val accessToken: String, val refreshToken: String)