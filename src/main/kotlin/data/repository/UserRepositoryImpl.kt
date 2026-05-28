package data.repository

import data.table.UserTable
import domain.model.User
import domain.repository.UserRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import utils.dbQuery

class UserRepositoryImpl : UserRepository {

    private fun rowToUser(row: ResultRow) = User(
        id = row[UserTable.id],
        username = row[UserTable.username],
        email = row[UserTable.email],
        role = row[UserTable.role]
    )

    private fun rowToUserWithHash(row: ResultRow) = UserWithHash(
        id = row[UserTable.id],
        username = row[UserTable.username],
        email = row[UserTable.email],
        passwordHash = row[UserTable.passwordHash],
        role = row[UserTable.role]
    )

    override suspend fun findAll(): List<User> = dbQuery {
        UserTable.selectAll().map(::rowToUser)
    }

    override suspend fun findById(id: Int): User? = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.id eq id }
            .map(::rowToUser)
            .singleOrNull()
    }

    override suspend fun findByUsername(username: String): User? = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.username eq username }
            .map(::rowToUser)
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .map(::rowToUser)
            .singleOrNull()
    }

    override suspend fun create(username: String, email: String, passwordHash: String, role: String): User = dbQuery {
        val id = UserTable.insert {
            it[UserTable.username] = username
            it[UserTable.email] = email
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.role] = role
        } get UserTable.id

        User(id, username, email, role)
    }

    override suspend fun update(
        id: Int,
        username: String?,
        email: String?,
        passwordHash: String?,
        role: String?
    ): User? = dbQuery {
        val updated = UserTable.update(
            where = { UserTable.id eq id }
        ) { stmt ->
            username?.let { stmt[UserTable.username] = it }
            email?.let { stmt[UserTable.email] = it }
            passwordHash?.let { stmt[UserTable.passwordHash] = it }
            role?.let { stmt[UserTable.role] = it }
        }
        if (updated == 0) null else findById(id)
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        UserTable.deleteWhere { UserTable.id eq id } > 0
    }

    override suspend fun existsByUsername(username: String): Boolean = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.username eq username }
            .count() > 0
    }

    override suspend fun existsByEmail(email: String): Boolean = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.email eq email }
            .count() > 0
    }

    suspend fun findByUsernameWithHash(username: String): UserWithHash? = dbQuery {
        UserTable
            .selectAll()
            .where { UserTable.username eq username }
            .map(::rowToUserWithHash)
            .singleOrNull()
    }
}

data class UserWithHash(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val role: String
)
