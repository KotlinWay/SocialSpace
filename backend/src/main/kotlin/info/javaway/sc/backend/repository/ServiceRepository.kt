package info.javaway.sc.backend.repository

import info.javaway.sc.backend.data.tables.Services
import info.javaway.sc.backend.models.Service
import info.javaway.sc.backend.models.ServiceStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.ExperimentalTime

/**
 * Repository для работы с услугами в базе данных
 */
class ServiceRepository {

    /**
     * Создать новую услугу
     */
    @OptIn(ExperimentalTime::class)
    fun createService(
        userId: Long,
        title: String,
        description: String,
        categoryId: Long,
        price: String?,
        images: List<String>
    ): Service? = transaction {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val imagesJson = Json.encodeToString(images)

        val insertStatement = Services.insert {
            it[Services.userId] = userId
            it[Services.title] = title
            it[Services.description] = description
            it[Services.categoryId] = categoryId
            it[Services.price] = price
            it[Services.images] = imagesJson
            it[Services.status] = ServiceStatus.ACTIVE
            it[Services.views] = 0
            it[Services.createdAt] = now
            it[Services.updatedAt] = now
        }

        insertStatement.resultedValues?.singleOrNull()?.let { rowToService(it) }
    }

    /**
     * Найти услугу по ID
     */
    fun findById(serviceId: Long): Service? = transaction {
        Services.selectAll()
            .where { Services.id eq serviceId }
            .map { rowToService(it) }
            .singleOrNull()
    }

    /**
     * Найти все услуги пользователя
     */
    fun findByUserId(userId: Long, limit: Int = 100, offset: Long = 0): List<Service> = transaction {
        Services.selectAll()
            .where { Services.userId eq userId }
            .limit(limit)
            .offset(offset)
            .orderBy(Services.createdAt to SortOrder.DESC)
            .map { rowToService(it) }
    }

    /**
     * Получить все услуги с фильтрацией и пагинацией
     */
    fun getAllServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        searchQuery: String? = null,
        limit: Int = 20,
        offset: Long = 0
    ): List<Service> = transaction {
        var query = Services.selectAll()

        // Фильтр по категории
        categoryId?.let { query = query.where { Services.categoryId eq it } }

        // Фильтр по статусу
        status?.let { query = query.where { Services.status eq it } }

        // Поиск по названию и описанию
        searchQuery?.let { search ->
            val searchPattern = "%${search.lowercase()}%"
            query = query.where {
                (Services.title.lowerCase() like searchPattern) or
                (Services.description.lowerCase() like searchPattern)
            }
        }

        query
            .limit(limit)
            .offset(offset)
            .orderBy(Services.createdAt to SortOrder.DESC)
            .map { rowToService(it) }
    }

    /**
     * Подсчитать общее количество услуг с учетом фильтров
     */
    fun countServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        searchQuery: String? = null
    ): Long = transaction {
        var query = Services.selectAll()

        categoryId?.let { query = query.where { Services.categoryId eq it } }
        status?.let { query = query.where { Services.status eq it } }

        searchQuery?.let { search ->
            val searchPattern = "%${search.lowercase()}%"
            query = query.where {
                (Services.title.lowerCase() like searchPattern) or
                (Services.description.lowerCase() like searchPattern)
            }
        }

        query.count()
    }

    /**
     * Обновить услугу
     */
    @OptIn(ExperimentalTime::class)
    fun updateService(
        serviceId: Long,
        title: String? = null,
        description: String? = null,
        categoryId: Long? = null,
        price: String? = null,
        status: ServiceStatus? = null,
        images: List<String>? = null
    ): Service? = transaction {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)

        Services.update({ Services.id eq serviceId }) {
            title?.let { value -> it[Services.title] = value }
            description?.let { value -> it[Services.description] = value }
            categoryId?.let { value -> it[Services.categoryId] = value }
            price?.let { value -> it[Services.price] = value }
            status?.let { value -> it[Services.status] = value }
            images?.let { value -> it[Services.images] = Json.encodeToString(value) }
            it[Services.updatedAt] = now
        }

        findById(serviceId)
    }

    /**
     * Удалить услугу
     */
    fun deleteService(serviceId: Long): Boolean = transaction {
        Services.deleteWhere { Services.id eq serviceId } > 0
    }

    /**
     * Увеличить счетчик просмотров услуги
     */
    fun incrementViews(serviceId: Long): Service? = transaction {
        Services.update({ Services.id eq serviceId }) {
            it[Services.views] = Services.views + 1
        }

        findById(serviceId)
    }

    /**
     * Проверить, принадлежит ли услуга пользователю
     */
    fun isOwner(userId: Long, serviceId: Long): Boolean = transaction {
        Services.selectAll()
            .where { (Services.id eq serviceId) and (Services.userId eq userId) }
            .count() > 0
    }

    /**
     * Преобразовать строку БД в модель Service
     */
    private fun rowToService(row: ResultRow): Service {
        val imagesJson = row[Services.images]
        val images = try {
            Json.decodeFromString<List<String>>(imagesJson)
        } catch (e: Exception) {
            emptyList()
        }

        return Service(
            id = row[Services.id].value,
            userId = row[Services.userId].value,
            title = row[Services.title],
            description = row[Services.description],
            categoryId = row[Services.categoryId].value,
            price = row[Services.price],
            images = images,
            status = row[Services.status],
            views = row[Services.views],
            createdAt = row[Services.createdAt].toString(),
            updatedAt = row[Services.updatedAt].toString()
        )
    }
}
