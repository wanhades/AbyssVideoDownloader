package com.abmo.util

import java.net.URI

fun String.extractReferer(): String? {
    return try {
        val url = URI(this).toURL()
        "${url.protocol}://${url.host}/"
    } catch (e: Exception) {
        null
    }
}


fun String.getHost(): String {
    return try {
        URI(this).toURL().host
    } catch (e: IllegalArgumentException) {
        this
    }
}

fun String.isValidUrl(): Boolean {
    return try {
        URI(this).toURL()
        true
    } catch (e: Exception) {
        false
    }
}

fun String.getParameter(name: String): String? {
    val regex = Regex("""[?&]$name=([^&]+)""", RegexOption.IGNORE_CASE)
    val matchResult = regex.find(this)
    return matchResult?.groupValues?.get(1)
}