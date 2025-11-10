package info.javaway.sc.backend.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: info.javaway.sc.backend.utils.JwtConfig.SECRET
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: info.javaway.sc.backend.utils.JwtConfig.ISSUER
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: info.javaway.sc.backend.utils.JwtConfig.AUDIENCE
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "SocialSpace"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                println("🔐 JWT Validation:")
                println("   Audience: ${credential.payload.audience}")
                println("   Issuer: ${credential.payload.issuer}")
                println("   UserId claim: ${credential.payload.getClaim("userId").asLong()}")

                if (credential.payload.getClaim("userId").asLong() != null) {
                    println("   ✅ JWT token valid")
                    JWTPrincipal(credential.payload)
                } else {
                    println("   ❌ JWT token invalid: userId claim is null")
                    null
                }
            }
            challenge { _, _ ->
                println("❌ JWT Authentication failed - no valid token provided")
            }
        }
    }
}
