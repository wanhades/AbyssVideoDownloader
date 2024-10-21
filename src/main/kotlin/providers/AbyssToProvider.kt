package com.abmo.providers


class AbyssToProvider: Provider {

    override fun getVideoID(url: String): String {
        return when {
            url.startsWith("https://abysscdn.com/?v=") -> url.substringAfter("v=")
            url.startsWith("https://short.ink/") -> url.substringAfterLast("/")
            else -> url
        }
    }
}