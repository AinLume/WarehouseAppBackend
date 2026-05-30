package domain.repository

import domain.model.User

interface UserRepository {
    suspend fun findAll(): List<User>
    suspend fun findById(id: Int): User?
    suspend fun findByUsername(username: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun create(username: String, email: String, passwordHash: String, role: String): User
    suspend fun update(id: Int, username: String?, email: String?, passwordHash: String?, role: String?): User?
    suspend fun delete(id: Int): Boolean
    suspend fun existsByUsername(username: String): Boolean
    suspend fun existsByEmail(email: String): Boolean
}
