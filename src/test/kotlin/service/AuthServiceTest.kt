package service

import api.dto.LoginRequest
import api.dto.RegisterRequest
import api.dto.UserResponse
import data.repository.UserRepositoryImpl
import data.repository.UserWithHash
import domain.model.UserRole
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuthServiceTest {

    private val userRepository: UserRepositoryImpl = mockk()
    private val jwtService: JwtService = mockk()

    private fun initService() = AuthService(userRepository, jwtService)

    @Test
    fun `register should create user and return tokens when valid data`() = runTest {
        val request = RegisterRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            role = "OWNER"
        )
        val user = domain.model.User(1, "testuser", "test@example.com", "OWNER")
        val tokens = TokenPair("access-token", "refresh-token")

        coEvery { userRepository.existsByUsername("testuser") } returns false
        coEvery { userRepository.existsByEmail("test@example.com") } returns false
        coEvery {
            userRepository.create("testuser", "test@example.com", any(), "OWNER")
        } returns user
        coEvery {
            jwtService.generateTokenPair(1, "testuser", "OWNER")
        } returns tokens
        val service = initService()

        val result = service.register(request)

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals(1, result.user.id)
        assertEquals("testuser", result.user.username)

        coVerify { userRepository.existsByUsername("testuser") }
        coVerify { userRepository.existsByEmail("test@example.com") }
        coVerify { userRepository.create("testuser", "test@example.com", any(), "OWNER") }
        coVerify { jwtService.generateTokenPair(1, "testuser", "OWNER") }
    }

    @Test
    fun `register should throw IllegalArgumentException when username is too short`() = runTest {
        val request = RegisterRequest(
            username = "ab",
            email = "test@example.com",
            password = "password123"
        )
        val service = initService()

        try {
            service.register(request)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Username must be at least 3 characters", e.message)
        }
        coVerify(exactly = 0) { userRepository.create(any(), any(), any(), any()) }
    }

    @Test
    fun `register should throw IllegalArgumentException when email is invalid`() = runTest {
        val request = RegisterRequest(
            username = "testuser",
            email = "invalid-email",
            password = "password123"
        )
        val service = initService()

        try {
            service.register(request)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid email", e.message)
        }
    }

    @Test
    fun `register should throw IllegalArgumentException when password is too short`() = runTest {
        val request = RegisterRequest(
            username = "testuser",
            email = "test@example.com",
            password = "12345"
        )
        val service = initService()

        try {
            service.register(request)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Password must be at least 6 characters", e.message)
        }
    }

    @Test
    fun `register should throw IllegalArgumentException when username already exists`() = runTest {
        val request = RegisterRequest(
            username = "existinguser",
            email = "test@example.com",
            password = "password123"
        )
        coEvery { userRepository.existsByUsername("existinguser") } returns true
        val service = initService()

        try {
            service.register(request)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Username already exists", e.message)
        }
        coVerify { userRepository.existsByUsername("existinguser") }
        coVerify(exactly = 0) { userRepository.create(any(), any(), any(), any()) }
    }

    @Test
    fun `register should throw IllegalArgumentException when email already exists`() = runTest {
        val request = RegisterRequest(
            username = "testuser",
            email = "existing@example.com",
            password = "password123"
        )
        coEvery { userRepository.existsByUsername("testuser") } returns false
        coEvery { userRepository.existsByEmail("existing@example.com") } returns true
        val service = initService()

        try {
            service.register(request)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Email already exists", e.message)
        }
    }

    @Test
    fun `login should return tokens when credentials are valid`() = runTest {
        mockkStatic("org.mindrot.jbcrypt.BCrypt")
        every { BCrypt.checkpw(any(), any()) } returns true

        val request = LoginRequest(username = "testuser", password = "password")
        val userWithHash = UserWithHash(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            role = "OWNER"
        )
        val tokens = TokenPair("access-token", "refresh-token")

        coEvery { userRepository.findByUsernameWithHash("testuser") } returns userWithHash
        coEvery { jwtService.generateTokenPair(1, "testuser", "OWNER") } returns tokens
        val service = initService()

        val result = service.login(request)

        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals(1, result.user.id)

        coVerify { userRepository.findByUsernameWithHash("testuser") }
        coVerify { jwtService.generateTokenPair(1, "testuser", "OWNER") }

        unmockkStatic("org.mindrot.jbcrypt.BCrypt")
    }

    @Test
    fun `login should throw IllegalArgumentException when user not found`() = runTest {
        val request = LoginRequest(username = "nonexistent", password = "password123")
        coEvery { userRepository.findByUsernameWithHash("nonexistent") } returns null
        val service = initService()

        try {
            service.login(request)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid username or password", e.message)
        }
    }

    @Test
    fun `me should return user when exists`() = runTest {
        val user = domain.model.User(1, "testuser", "test@example.com", "OWNER")
        coEvery { userRepository.findById(1) } returns user
        val service = initService()

        val result = service.me(1)

        assertEquals(1, result.id)
        assertEquals("testuser", result.username)
        assertEquals("test@example.com", result.email)
        assertEquals("OWNER", result.role)

        coVerify { userRepository.findById(1) }
    }

    @Test
    fun `me should throw IllegalArgumentException when user not found`() = runTest {
        coEvery { userRepository.findById(1) } returns null
        val service = initService()

        try {
            service.me(1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("User not found", e.message)
        }
    }
}
