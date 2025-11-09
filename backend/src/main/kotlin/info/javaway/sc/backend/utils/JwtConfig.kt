package info.javaway.sc.backend.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET = "your-secret-key-change-in-production"
    private const val ISSUER = "http://0.0.0.0:8080"
    private const val AUDIENCE = "http://0.0.0.0:8080"
    private const val VALIDITY_IN_MS = 36_000_00 * 24 * 7 // 7 days

    fun makeToken(userId: Long): String = JWT.create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
        .sign(Algorithm.HMAC256(SECRET))
}
