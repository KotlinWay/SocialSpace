package info.javaway.sc.backend.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Favorites : LongIdTable("favorites") {
    val userId = reference("user_id", Users)
    val productId = reference("product_id", Products)
    val createdAt = timestamp("created_at").default(Instant.now())

    init {
        uniqueIndex(userId, productId)
    }
}
