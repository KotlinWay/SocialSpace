package com.socialspace.server.utils

import com.socialspace.server.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Фабрика для инициализации и управления подключением к базе данных
 */
object DatabaseFactory {

    /**
     * Инициализация подключения к базе данных
     *
     * @param jdbcUrl URL подключения к БД (например: jdbc:postgresql://localhost:5432/socialspace)
     * @param driverClassName Класс драйвера JDBC (org.postgresql.Driver)
     * @param username Имя пользователя БД
     * @param password Пароль пользователя БД
     */
    fun init(
        jdbcUrl: String,
        driverClassName: String,
        username: String,
        password: String
    ) {
        val database = Database.connect(createHikariDataSource(
            url = jdbcUrl,
            driver = driverClassName,
            username = username,
            password = password
        ))

        // Создание таблиц при первом запуске (миграция)
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    /**
     * Создание пула соединений HikariCP
     */
    private fun createHikariDataSource(
        url: String,
        driver: String,
        username: String,
        password: String
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        this.username = username
        this.password = password
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    /**
     * Выполнение запроса к БД в suspend-функции
     * Используется для корутин
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
