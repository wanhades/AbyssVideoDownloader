package util

import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException
import java.net.URI

fun Any.toJson(): String = Gson().toJson(this)

fun systemDownloadFolder(): File {
    val home = System.getProperty("user.home")
    return File(home, "Downloads")
}

fun String.getVideoID(): String {
    return this.substringAfter("v=")
}

fun String.extractReferer(): String? {
    return try {
        val url = URI(this).toURL()
        "${url.protocol}://${url.host}/"
    } catch (e: Exception) {
        null
    }
}

fun String.isValidUrl(): Boolean {
    return try {
        URI(this).toURL() // Try to construct a URL object
        true      // If successful, it's a valid URL
    } catch (e: Exception) {
        false     // If an exception is thrown, it's not a valid URL
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
