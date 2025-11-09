package info.javaway.sc.backend.plugins

import info.javaway.sc.backend.data.tables.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val driverClassName = environment.config.propertyOrNull("storage.driverClassName")?.getString()
        ?: "org.h2.Driver"
    val jdbcURL = environment.config.propertyOrNull("storage.jdbcURL")?.getString()
        ?: "jdbc:h2:file:./build/db/socialspace;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

    // Для H2 user и password не обязательны
    val user = environment.config.propertyOrNull("storage.user")?.getString()
    val password = environment.config.propertyOrNull("storage.password")?.getString()

    Database.connect(
        url = jdbcURL,
        driver = driverClassName,
        user = user ?: "",
        password = password ?: ""
    )

    transaction {
        // Create tables if they don't exist
        SchemaUtils.create(Users, Categories, Products, Services, Favorites)

        environment.log.info("Database configured: $jdbcURL")
        environment.log.info("Tables created successfully")
    }
}
