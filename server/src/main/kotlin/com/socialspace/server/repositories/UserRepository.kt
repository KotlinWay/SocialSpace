package com.socialspace.server.repositories

import com.socialspace.server.models.*
import com.socialspace.server.utils.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

/**
 * Репозиторий для работы с пользователями
 * Инкапсулирует всю логику работы с БД для сущности User
 */
class UserRepository {

    /**
     * Создать нового пользователя
     */
    suspend fun create(createUserDTO: CreateUserDTO): UserDTO? = dbQuery {
        val insertStatement = Users.insert {
            it[phone] = createUserDTO.phone
            it[email] = createUserDTO.email
            it[name] = createUserDTO.name
            // TODO: Добавить хеширование пароля когда будет Auth
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUserDTO)
    }

    /**
     * Получить пользователя по ID
     */
    suspend fun findById(id: UUID): UserDTO? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map(::resultRowToUserDTO)
            .singleOrNull()
    }

    /**
     * Получить пользователя по номеру телефона
     */
    suspend fun findByPhone(phone: String): UserDTO? = dbQuery {
        Users.selectAll()
            .where { Users.phone eq phone }
            .map(::resultRowToUserDTO)
            .singleOrNull()
    }

    /**
     * Получить пользователя по email
     */
    suspend fun findByEmail(email: String): UserDTO? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .map(::resultRowToUserDTO)
            .singleOrNull()
    }

    /**
     * Получить всех пользователей
     */
    suspend fun findAll(): List<UserDTO> = dbQuery {
        Users.selectAll().map(::resultRowToUserDTO)
    }

    /**
     * Обновить профиль пользователя
     */
    suspend fun update(id: UUID, updateUserDTO: UpdateUserDTO): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            updateUserDTO.name?.let { name -> it[Users.name] = name }
            updateUserDTO.email?.let { email -> it[Users.email] = email }
            updateUserDTO.bio?.let { bio -> it[Users.bio] = bio }
            updateUserDTO.avatar?.let { avatar -> it[Users.avatar] = avatar }
        } > 0
    }

    /**
     * Обновить время последней активности пользователя
     */
    suspend fun updateLastActive(id: UUID): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[lastActive] = Instant.now()
        } > 0
    }

    /**
     * Обновить рейтинг пользователя
     */
    suspend fun updateRating(id: UUID, rating: Float): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[Users.rating] = rating
        } > 0
    }

    /**
     * Удалить пользователя по ID
     */
    suspend fun delete(id: UUID): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

    /**
     * Проверить, существует ли пользователь с таким телефоном
     */
    suspend fun existsByPhone(phone: String): Boolean = dbQuery {
        Users.selectAll()
            .where { Users.phone eq phone }
            .count() > 0
    }

    /**
     * Проверить, существует ли пользователь с таким email
     */
    suspend fun existsByEmail(email: String): Boolean = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .count() > 0
    }

    /**
     * Преобразование ResultRow в UserDTO
     */
    private fun resultRowToUserDTO(row: ResultRow) = UserDTO(
        id = row[Users.id].toString(),
        phone = row[Users.phone],
        email = row[Users.email],
        name = row[Users.name],
        avatar = row[Users.avatar],
        bio = row[Users.bio],
        rating = row[Users.rating],
        createdAt = row[Users.createdAt].toString(),
        lastActive = row[Users.lastActive].toString(),
        role = row[Users.role]
    )
}
