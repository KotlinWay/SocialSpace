package info.javaway.sc.backend.repository

import info.javaway.sc.backend.data.tables.Users
import info.javaway.sc.backend.models.User
import info.javaway.sc.backend.models.UserRole
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Repository для работы с пользователями в базе данных
 */
class UserRepository {

    /**
     * Создать нового пользователя
     */
    fun createUser(
        phone: String,
        email: String?,
        name: String,
        passwordHash: String,
        role: UserRole = UserRole.USER
    ): User? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val insertStatement = Users.insert {
            it[Users.phone] = phone
            it[Users.email] = email
            it[Users.name] = name
            it[Users.passwordHash] = passwordHash
            it[Users.createdAt] = now
            it[Users.role] = role
        }

        insertStatement.resultedValues?.singleOrNull()?.let { rowToUser(it) }
    }

    /**
     * Найти пользователя по ID
     */
    fun findById(userId: Long): User? = transaction {
        Users.selectAll()
            .where { Users.id eq userId }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    /**
     * Найти пользователя по номеру телефона
     */
    fun findByPhone(phone: String): User? = transaction {
        Users.selectAll()
            .where { Users.phone eq phone }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    /**
     * Найти пользователя по email
     */
    fun findByEmail(email: String): User? = transaction {
        Users.selectAll()
            .where { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    /**
     * Получить хеш пароля пользователя по телефону
     */
    fun getPasswordHash(phone: String): String? = transaction {
        Users.select(Users.passwordHash)
            .where { Users.phone eq phone }
            .map { it[Users.passwordHash] }
            .singleOrNull()
    }

    /**
     * Обновить профиль пользователя
     */
    fun updateUser(
        userId: Long,
        name: String? = null,
        email: String? = null,
        bio: String? = null,
        avatar: String? = null
    ): User? = transaction {
        Users.update({ Users.id eq userId }) {
            name?.let { value -> it[Users.name] = value }
            email?.let { value -> it[Users.email] = value }
            bio?.let { value -> it[Users.bio] = value }
            avatar?.let { value -> it[Users.avatar] = value }
        }

        findById(userId)
    }

    /**
     * Обновить аватар пользователя
     */
    fun updateAvatar(userId: Long, avatarUrl: String): User? = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.avatar] = avatarUrl
        }

        findById(userId)
    }

    /**
     * Обновить рейтинг пользователя
     */
    fun updateRating(userId: Long, rating: Double): User? = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.rating] = rating
        }

        findById(userId)
    }

    /**
     * Проверить, верифицирован ли пользователь
     */
    fun setVerified(userId: Long, verified: Boolean): User? = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.isVerified] = verified
        }

        findById(userId)
    }

    /**
     * Удалить пользователя
     */
    fun deleteUser(userId: Long): Boolean = transaction {
        Users.deleteWhere { Users.id eq userId } > 0
    }

    /**
     * Проверить существование пользователя по телефону
     */
    fun existsByPhone(phone: String): Boolean = transaction {
        Users.selectAll()
            .where { Users.phone eq phone }
            .count() > 0
    }

    /**
     * Проверить существование пользователя по email
     */
    fun existsByEmail(email: String): Boolean = transaction {
        Users.selectAll()
            .where { Users.email eq email }
            .count() > 0
    }

    /**
     * Получить всех пользователей (для админки)
     */
    fun getAllUsers(limit: Int = 100, offset: Long = 0): List<User> = transaction {
        Users.selectAll()
            .limit(limit, offset)
            .orderBy(Users.createdAt to SortOrder.DESC)
            .map { rowToUser(it) }
    }

    /**
     * Преобразовать строку БД в модель User
     */
    private fun rowToUser(row: ResultRow): User {
        return User(
            id = row[Users.id].value,
            phone = row[Users.phone],
            email = row[Users.email],
            name = row[Users.name],
            avatar = row[Users.avatar],
            bio = row[Users.bio],
            rating = row[Users.rating],
            createdAt = row[Users.createdAt].toString(),
            isVerified = row[Users.isVerified],
            role = row[Users.role]
        )
    }
}
