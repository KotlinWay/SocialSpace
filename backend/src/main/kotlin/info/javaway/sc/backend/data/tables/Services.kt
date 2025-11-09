package info.javaway.sc.backend.data.tables

import info.javaway.sc.backend.models.ServiceStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Services : LongIdTable("services") {
    val userId = reference("user_id", Users)
    val title = varchar("title", 200)
    val description = text("description")
    val categoryId = reference("category_id", Categories)
    val price = varchar("price", 100).nullable() // "1000" или "Договорная"
    val images = text("images") // JSON array of image URLs
    val status = enumerationByName("status", 20, ServiceStatus::class).default(ServiceStatus.ACTIVE)
    val views = integer("views").default(0)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
