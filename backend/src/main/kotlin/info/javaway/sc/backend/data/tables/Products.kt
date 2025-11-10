package info.javaway.sc.backend.data.tables

import info.javaway.sc.backend.models.ProductCondition
import info.javaway.sc.backend.models.ProductStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Products : LongIdTable("products") {
    val userId = reference("user_id", Users)
    val title = varchar("title", 200)
    val description = text("description")
    val price = double("price")
    val categoryId = reference("category_id", Categories)
    val condition = enumerationByName("condition", 20, ProductCondition::class)
    val images = text("images") // JSON array of image URLs
    val status = enumerationByName("status", 20, ProductStatus::class).default(ProductStatus.ACTIVE)
    val views = integer("views").default(0)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
}
