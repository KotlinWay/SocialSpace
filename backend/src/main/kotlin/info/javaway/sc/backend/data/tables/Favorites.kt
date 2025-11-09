package info.javaway.sc.backend.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Favorites : LongIdTable("favorites") {
    val userId = reference("user_id", Users)
    val productId = reference("product_id", Products)
    val createdAt = datetime("created_at")

    init {
        uniqueIndex(userId, productId)
    }
}
