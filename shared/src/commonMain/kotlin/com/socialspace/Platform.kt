package com.socialspace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
