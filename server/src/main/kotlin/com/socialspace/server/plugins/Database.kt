package com.socialspace.server.plugins

import com.socialspace.server.utils.DatabaseFactory
import io.ktor.server.application.*

/**
 * Конфигурация базы данных для приложения
 */
fun Application.configureDatabase() {
    val config = environment.config

    // Получаем настройки БД из application.conf
    val jdbcUrl = config.propertyOrNull("database.jdbcUrl")?.getString()
        ?: System.getenv("DB_URL")
        ?: "jdbc:postgresql://localhost:5432/socialspace"

    val driverClassName = config.propertyOrNull("database.driverClassName")?.getString()
        ?: "org.postgresql.Driver"

    val username = config.propertyOrNull("database.username")?.getString()
        ?: System.getenv("DB_USER")
        ?: "postgres"

    val password = config.propertyOrNull("database.password")?.getString()
        ?: System.getenv("DB_PASSWORD")
        ?: "postgres"

    // Инициализация базы данных
    DatabaseFactory.init(
        jdbcUrl = jdbcUrl,
        driverClassName = driverClassName,
        username = username,
        password = password
    )

    log.info("Database initialized successfully: $jdbcUrl")
}
