package service
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.*

class JwtService(
    privateKeyContent: String,
    publicKeyContent: String,
    val issuer: String,
    val audience: String,
    private val accessTokenLifetimeMs: Long = 1000 * 60 * 15,
    private val refreshTokenLifetimeMs: Long = 1000 * 60 * 60 * 24 * 7
) {
    private val privateKey: RSAPrivateKey = parsePrivateKey(privateKeyContent)
    val publicKey: RSAPublicKey = parsePublicKey(publicKeyContent)

    private fun parsePrivateKey(keyContent: String): RSAPrivateKey {
        val cleanedKey = keyContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(cleanedKey)
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec) as RSAPrivateKey
    }

    private fun parsePublicKey(keyContent: String): RSAPublicKey {
        val cleanedKey = keyContent
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.getDecoder().decode(cleanedKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec) as RSAPublicKey
    }

    private val signingAlgorithm: Algorithm = Algorithm.RSA256(null, privateKey)
    val verificationAlgorithm: Algorithm = Algorithm.RSA256(publicKey, null)

    fun generateAccessToken(userId: Int, username: String, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", role)
            .withClaim("type", "access")
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenLifetimeMs))
            .sign(signingAlgorithm)
    }

    fun generateRefreshToken(userId: Int): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenLifetimeMs))
            .sign(signingAlgorithm)
    }

    fun generateTokenPair(userId: Int, username: String, role: String): TokenPair {
        return TokenPair(
            accessToken = generateAccessToken(userId, username, role),
            refreshToken = generateRefreshToken(userId)
        )
    }
}

data class TokenPair(val accessToken: String, val refreshToken: String)