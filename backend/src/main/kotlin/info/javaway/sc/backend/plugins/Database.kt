package info.javaway.sc.backend.plugins

import info.javaway.sc.backend.data.tables.*
import info.javaway.sc.backend.repository.CategoryRepository
import info.javaway.sc.backend.services.SpaceBootstrapper
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
        val bootstrapper = SpaceBootstrapper(environment.log)

        SchemaUtils.createMissingTablesAndColumns(Users)
        SchemaUtils.createMissingTablesAndColumns(Categories)
        SchemaUtils.createMissingTablesAndColumns(Spaces)

        val (ownerId, spaceId) = bootstrapper.ensureDemoOwnerAndSpace()

        SchemaUtils.createMissingTablesAndColumns(SpaceMembers)
        SchemaUtils.createMissingTablesAndColumns(Products)
        SchemaUtils.createMissingTablesAndColumns(Services)
        SchemaUtils.createMissingTablesAndColumns(Favorites)

        bootstrapper.ensureMembershipsAndData(spaceId, ownerId)

        environment.log.info("Default space ensured: $spaceId")
        environment.log.info("Database configured: $jdbcURL")
        environment.log.info("Tables created/updated successfully")
    }

    // Populate default categories if needed
    val categoryRepository = CategoryRepository()
    categoryRepository.populateDefaultCategories()
    environment.log.info("Default categories populated")
}
