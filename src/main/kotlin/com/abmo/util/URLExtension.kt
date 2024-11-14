package com.abmo.util

import java.net.URI

/**
 * Extracts the referer URL from a given String URL.
 *
 * @return The referer URL as a String, or null if the URL is invalid.
 */
fun String.extractReferer(): String? {
    return try {
        val url = URI(this).toURL()
        "${url.protocol}://${url.host}/"
    } catch (e: Exception) {
        null
    }
}

/**
 * Retrieves the host from a given String URL.
 *
 * @return The host as a String, or the original String if the URL is invalid.
 */
fun String.getHost(): String {
    return try {
        URI(this).toURL().host
    } catch (e: IllegalArgumentException) {
        this
    }
}

/**
 * Checks if the String is a valid URL.
 *
 * @return True if the String is a valid URL; false otherwise.
 */
fun String.isValidUrl(): Boolean {
    return try {
        URI(this).toURL()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Retrieves the value of a query parameter from a URL.
 *
 * @param name The name of the parameter to retrieve.
 * @return The parameter value as a String, or null if not found.
 */
fun String.getParameter(name: String): String? {
    val regex = Regex("""[?&]$name=([^&]+)""", RegexOption.IGNORE_CASE)
    val matchResult = regex.find(this)
    return matchResult?.groupValues?.get(1)
}