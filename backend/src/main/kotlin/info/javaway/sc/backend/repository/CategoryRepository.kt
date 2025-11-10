package info.javaway.sc.backend.repository

import info.javaway.sc.backend.data.tables.Categories
import info.javaway.sc.api.models.Category
import info.javaway.sc.api.models.CategoryType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Repository для работы с категориями в базе данных
 */
class CategoryRepository {

    /**
     * Получить все категории
     */
    fun getAllCategories(): List<Category> = transaction {
        Categories.selectAll()
            .orderBy(Categories.name to SortOrder.ASC)
            .map { rowToCategory(it) }
    }

    /**
     * Получить категории по типу (PRODUCT или SERVICE)
     */
    fun getCategoriesByType(type: CategoryType): List<Category> = transaction {
        Categories.selectAll()
            .where { Categories.type eq type }
            .orderBy(Categories.name to SortOrder.ASC)
            .map { rowToCategory(it) }
    }

    /**
     * Найти категорию по ID
     */
    fun findById(categoryId: Long): Category? = transaction {
        Categories.selectAll()
            .where { Categories.id eq categoryId }
            .map { rowToCategory(it) }
            .singleOrNull()
    }

    /**
     * Создать новую категорию
     */
    fun createCategory(
        name: String,
        type: CategoryType,
        icon: String? = null
    ): Category? = transaction {
        val insertStatement = Categories.insert {
            it[Categories.name] = name
            it[Categories.type] = type
            it[Categories.icon] = icon
        }

        insertStatement.resultedValues?.singleOrNull()?.let { rowToCategory(it) }
    }

    /**
     * Проверить существование категории по имени и типу
     */
    fun existsByNameAndType(name: String, type: CategoryType): Boolean = transaction {
        Categories.selectAll()
            .where { (Categories.name eq name) and (Categories.type eq type) }
            .count() > 0
    }

    /**
     * Предзаполнение категорий (seed data)
     * Вызывается один раз при инициализации БД
     */
    fun populateDefaultCategories() = transaction {
        // Проверяем, есть ли уже категории
        if (Categories.selectAll().count() > 0) {
            return@transaction
        }

        // Категории товаров
        val productCategories = listOf(
            "Мебель" to "🪑",
            "Электроника" to "📱",
            "Детские товары" to "🧸",
            "Одежда и обувь" to "👕",
            "Спорт и отдых" to "⚽",
            "Книги и журналы" to "📚",
            "Для дома" to "🏠",
            "Сад и огород" to "🌱",
            "Стройматериалы" to "🔨",
            "Автотовары" to "🚗",
            "Бытовая техника" to "🔌",
            "Красота и здоровье" to "💄",
            "Продукты питания" to "🍎",
            "Животные" to "🐾",
            "Разное" to "📦"
        )

        productCategories.forEach { (name, icon) ->
            Categories.insert {
                it[Categories.name] = name
                it[Categories.type] = CategoryType.PRODUCT
                it[Categories.icon] = icon
            }
        }

        // Категории услуг
        val serviceCategories = listOf(
            "Ремонт и строительство" to "🔧",
            "Уборка" to "🧹",
            "Репетиторство" to "📖",
            "Красота и здоровье" to "💇",
            "Доставка и перевозки" to "🚚",
            "Няня и сиделка" to "👶",
            "Компьютерная помощь" to "💻",
            "Сад и огород" to "🌿",
            "Фото и видео" to "📸",
            "Организация мероприятий" to "🎉",
            "Юридические услуги" to "⚖️",
            "Финансовые услуги" to "💰",
            "Ветеринарные услуги" to "🐕",
            "Автомобильные услуги" to "🚘",
            "Разное" to "🛠️"
        )

        serviceCategories.forEach { (name, icon) ->
            Categories.insert {
                it[Categories.name] = name
                it[Categories.type] = CategoryType.SERVICE
                it[Categories.icon] = icon
            }
        }
    }

    /**
     * Преобразовать строку БД в модель Category
     */
    private fun rowToCategory(row: ResultRow): Category {
        return Category(
            id = row[Categories.id].value,
            name = row[Categories.name],
            icon = row[Categories.icon],
            type = row[Categories.type]
        )
    }
}
