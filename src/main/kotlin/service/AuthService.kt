package service

import api.dto.AuthResponse
import api.dto.LoginRequest
import api.dto.RegisterRequest
import api.dto.UserResponse
import api.mappers.toResponse
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import data.repository.UserRepositoryImpl
import domain.model.User
import domain.model.UserRole
import org.mindrot.jbcrypt.BCrypt

class AuthService(
    private val userRepository: UserRepositoryImpl,
    private val jwtService: JwtService
) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        val trimmedUsername = request.username.trim()
        val trimmedEmail = request.email.trim()

        if (trimmedUsername.isBlank() || trimmedUsername.length < 3)
            throw IllegalArgumentException("Username must be at least 3 characters")
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@"))
            throw IllegalArgumentException("Invalid email")
        if (request.password.isBlank() || request.password.length < 6)
            throw IllegalArgumentException("Password must be at least 6 characters")

        if (userRepository.existsByUsername(trimmedUsername))
            throw IllegalArgumentException("Username already exists")
        if (userRepository.existsByEmail(trimmedEmail))
            throw IllegalArgumentException("Email already exists")

        val role = try {
            UserRole.fromString(request.role).name
        } catch (e: IllegalArgumentException) {
            UserRole.OWNER.name
        }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = userRepository.create(trimmedUsername, trimmedEmail, passwordHash, role)

        val tokens = jwtService.generateTokenPair(user.id, user.username, user.role)

        return AuthResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            user = user.toResponse()
        )
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val userWithHash = userRepository.findByUsernameWithHash(request.username.trim())
            ?: throw IllegalArgumentException("Invalid username or password")

        if (!BCrypt.checkpw(request.password, userWithHash.passwordHash))
            throw IllegalArgumentException("Invalid username or password")

        val user = UserResponse(
            id = userWithHash.id,
            username = userWithHash.username,
            email = userWithHash.email,
            role = userWithHash.role
        )

        val tokens = jwtService.generateTokenPair(user.id, user.username, user.role)

        return AuthResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            user = user
        )
    }

    suspend fun refreshToken(refreshToken: String): AuthResponse {
        val verifier = JWT.require(jwtService.verificationAlgorithm)
            .withAudience(jwtService.audience)
            .withIssuer(jwtService.issuer)
            .build()

        val decoded = verifier.verify(refreshToken)

        val tokenType = decoded.getClaim("type").asString()
        if (tokenType != "refresh")
            throw IllegalArgumentException("Invalid token type")

        val userId = decoded.getClaim("userId").asInt()
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val tokens = jwtService.generateTokenPair(user.id, user.username, user.role)

        return AuthResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            user = user.toResponse()
        )
    }

    suspend fun me(userId: Int): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")
        return user.toResponse()
    }
}
