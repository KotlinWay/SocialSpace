package info.javaway.sc.backend.data.tables

import info.javaway.sc.backend.models.CategoryType
import org.jetbrains.exposed.dao.id.LongIdTable

object Categories : LongIdTable("categories") {
    val name = varchar("name", 100)
    val icon = varchar("icon", 500).nullable()
    val type = enumerationByName("type", 20, CategoryType::class)
}
