package com.abmo.util

import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileNotFoundException
import java.net.URI

fun Any.toJson(): String = Gson().toJson(this)


fun String.getVParameter(): String? {
    val regex = Regex("""[vV]=([^&]+)""")
    val matchResult = regex.find(this)
    return matchResult?.groupValues?.get(1)
}

fun String.extractReferer(): String? {
    return try {
        val url = URI(this).toURL()
        "${url.protocol}://${url.host}/"
    } catch (e: Exception) {
        null
    }
}

fun String.getBaseUrl(): String {
    val url = URI(this).toURL()
    return "${url.protocol}://${url.host}"
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

fun String.fetchDocument(): Document = Jsoup.connect(this).get()

fun String.toJsoupDocument(): Document = Jsoup.parse(this)

fun String.findValueByKey(key: String): String? {
    // Regex pattern to match "key": followed by any value (string, number, HTML, etc.)
    // It handles cases where the value might be a quoted string or not quoted (e.g., number, null, boolean)
    val regex = """"$key"\s*:\s*("(?:[^"\\]*(?:\\.[^"\\]*)*)"|[^\s,}]+)""".toRegex()

    // find matches in the JSON string
    val matchResult = regex.find(this)

    return matchResult?.groupValues?.get(1)?.let {
        if (it.startsWith("\"") && it.endsWith("\"")) {
            it.substring(1, it.length - 1)
        } else {
            it
        }
    }
}

fun formatBytes(bytes: Long?): String {
    if (bytes == null) return ""
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        bytes >= gigabyte -> String.format("%.2f GB", bytes / gigabyte)
        bytes >= megabyte -> String.format("%.2f MB", bytes / megabyte)
        bytes >= kilobyte -> String.format("%.2f KB", bytes / kilobyte)
        else -> "$bytes Bytes"
    }
}

fun Long.toReadableTime(): String {
    val totalSeconds = this / 1000
    val seconds = totalSeconds % 60
    val totalMinutes = totalSeconds / 60
    val minutes = totalMinutes % 60
    val hours = totalMinutes / 60

    return when {
        hours > 0 -> "$hours hours and $minutes minutes"
        minutes > 0 -> "$minutes minutes and $seconds seconds"
        else -> "$seconds seconds"
    }
}

fun isValidPath(filePath: String?): Boolean {
    filePath?.let { path ->
        return try {
            val outputFile = File(path).canonicalFile
            val outputDir = outputFile.parentFile

            if (path.endsWith("/") || path.endsWith("\\")) {
                println("Error: Invalid File path.")
                return false
            }

            if (outputDir != null) {
                if (!outputDir.exists()) {
                    println("Error: Output directory does not exist: ${outputDir.absolutePath}")
                    return false
                }
                if (!outputDir.isDirectory) {
                    println("Error: Output path is not a directory: ${outputDir.absolutePath}")
                    return false
                }
            }

            if (outputFile.name.isBlank()) {
                println("Error: No valid file name specified.")
                return false
            }
            if (!outputFile.name.endsWith(".mp4", ignoreCase = true)) {
                println("Error: File must have a .mp4 extension: ${outputFile.name}")
                return false
            }
            if (outputFile.exists()) {
                println("Error: File already exists: ${outputFile.absolutePath}")
                return false
            }

            true // all checks passed
        } catch (e: FileNotFoundException) {
            println("Error: Unable to access path: ${e.message}")
            false
        } catch (e: SecurityException) {
            println("Error: Access denied to path: ${path}. ${e.message}")
            false
        }
    }
    return true
}
