package service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtServiceTest {

    private fun generateTestKeyPair(): Pair<String, String> {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()

        val privateKey = keyPair.private as RSAPrivateKey
        val publicKey = keyPair.public as RSAPublicKey

        val privateKeyPem = """
            -----BEGIN PRIVATE KEY-----
            ${Base64.getEncoder().encodeToString(privateKey.encoded)
                .chunked(64)
                .joinToString("\n")}
            -----END PRIVATE KEY-----
        """.trimIndent()

        val publicKeyPem = """
            -----BEGIN PUBLIC KEY-----
            ${Base64.getEncoder().encodeToString(publicKey.encoded)
                .chunked(64)
                .joinToString("\n")}
            -----END PUBLIC KEY-----
        """.trimIndent()

        return privateKeyPem to publicKeyPem
    }

    @Test
    fun `generateAccessToken should return valid JWT token`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "test-issuer",
            audience = "test-audience",
            accessTokenLifetimeMs = 1000 * 60 * 15
        )

        val token = jwtService.generateAccessToken(1, "testuser", "OWNER")

        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        val decodedJWT = JWT.require(jwtService.verificationAlgorithm)
            .withAudience("test-audience")
            .withIssuer("test-issuer")
            .build()
            .verify(token)

        assertEquals(1, decodedJWT.getClaim("userId").asInt())
        assertEquals("testuser", decodedJWT.getClaim("username").asString())
        assertEquals("OWNER", decodedJWT.getClaim("role").asString())
        assertEquals("access", decodedJWT.getClaim("type").asString())
    }

    @Test
    fun `generateRefreshToken should return valid JWT token`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "test-issuer",
            audience = "test-audience"
        )

        val token = jwtService.generateRefreshToken(1)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        val decodedJWT = JWT.require(jwtService.verificationAlgorithm)
            .withAudience("test-audience")
            .withIssuer("test-issuer")
            .build()
            .verify(token)

        assertEquals(1, decodedJWT.getClaim("userId").asInt())
        assertEquals("refresh", decodedJWT.getClaim("type").asString())
    }

    @Test
    fun `generateRefreshToken should have longer expiration than access token`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "test-issuer",
            audience = "test-audience",
            accessTokenLifetimeMs = 1000 * 60 * 15,
            refreshTokenLifetimeMs = 1000 * 60 * 60 * 24 * 7
        )

        val accessToken = jwtService.generateAccessToken(1, "testuser", "OWNER")
        val refreshToken = jwtService.generateRefreshToken(1)

        val accessDecoded = JWT.require(jwtService.verificationAlgorithm).build().verify(accessToken)
        val refreshDecoded = JWT.require(jwtService.verificationAlgorithm).build().verify(refreshToken)

        assertTrue(refreshDecoded.expiresAt.time > accessDecoded.expiresAt.time)
    }

    @Test
    fun `generateTokenPair should return both access and refresh tokens`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "test-issuer",
            audience = "test-audience"
        )

        val tokenPair = jwtService.generateTokenPair(1, "testuser", "OWNER")

        assertNotNull(tokenPair.accessToken)
        assertNotNull(tokenPair.refreshToken)
        assertTrue(tokenPair.accessToken.isNotEmpty())
        assertTrue(tokenPair.refreshToken.isNotEmpty())

        val accessDecoded = JWT.require(jwtService.verificationAlgorithm).build().verify(tokenPair.accessToken)
        assertEquals("access", accessDecoded.getClaim("type").asString())
        assertEquals("testuser", accessDecoded.getClaim("username").asString())
        assertEquals("OWNER", accessDecoded.getClaim("role").asString())

        val refreshDecoded = JWT.require(jwtService.verificationAlgorithm).build().verify(tokenPair.refreshToken)
        assertEquals("refresh", refreshDecoded.getClaim("type").asString())
        assertEquals(1, refreshDecoded.getClaim("userId").asInt())
    }

    @Test
    fun `JwtService should use RSA256 algorithm for signing`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "test-issuer",
            audience = "test-audience"
        )

        val token = jwtService.generateAccessToken(1, "testuser", "OWNER")

        val decodedJWT = JWT.decode(token)
        assertEquals("RS256", decodedJWT.algorithm)
    }

    @Test
    fun `JwtService should verify tokens with public key`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "test-issuer",
            audience = "test-audience"
        )

        val token = jwtService.generateAccessToken(1, "testuser", "OWNER")

        val verifier = JWT.require(jwtService.verificationAlgorithm)
            .withAudience("test-audience")
            .withIssuer("test-issuer")
            .build()
        verifier.verify(token)
    }

    @Test
    fun `generated tokens should contain correct issuer and audience`() {
        val (privateKey, publicKey) = generateTestKeyPair()
        val jwtService = JwtService(
            privateKeyContent = privateKey,
            publicKeyContent = publicKey,
            issuer = "my-issuer",
            audience = "my-audience"
        )

        val token = jwtService.generateAccessToken(1, "testuser", "OWNER")

        val decodedJWT = JWT.decode(token)
        assertEquals("my-issuer", decodedJWT.issuer)
        assertEquals("my-audience", decodedJWT.audience.first())
    }
}
