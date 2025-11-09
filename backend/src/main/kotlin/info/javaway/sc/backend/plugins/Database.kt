package info.javaway.sc.backend.plugins

import info.javaway.sc.backend.data.tables.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val driverClassName = environment.config.propertyOrNull("storage.driverClassName")?.getString()
        ?: "org.postgresql.Driver"
    val jdbcURL = environment.config.propertyOrNull("storage.jdbcURL")?.getString()
        ?: "jdbc:postgresql://localhost:5432/socialspace"
    val user = environment.config.propertyOrNull("storage.user")?.getString()
        ?: "postgres"
    val password = environment.config.propertyOrNull("storage.password")?.getString()
        ?: "postgres"

    Database.connect(
        url = jdbcURL,
        driver = driverClassName,
        user = user,
        password = password
    )

    transaction {
        // Create tables if they don't exist
        SchemaUtils.create(Users, Categories, Products, Services, Favorites)
    }
}
