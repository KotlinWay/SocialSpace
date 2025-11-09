package info.javaway.sc.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val type: CategoryType
)

@Serializable
enum class CategoryType {
    PRODUCT,
    SERVICE
}
