package info.javaway.sc.backend.data.tables

import info.javaway.sc.api.models.ServiceStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Services : LongIdTable("services") {
    val userId = reference("user_id", Users)
    val title = varchar("title", 200)
    val description = text("description")
    val categoryId = reference("category_id", Categories)
    val price = varchar("price", 100).nullable() // "1000" или "Договорная"
    val images = text("images") // JSON array of image URLs
    val status = enumerationByName("status", 20, ServiceStatus::class).default(ServiceStatus.ACTIVE)
    val views = integer("views").default(0)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}
