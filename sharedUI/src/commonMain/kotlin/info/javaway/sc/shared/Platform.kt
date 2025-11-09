package info.javaway.sc.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
