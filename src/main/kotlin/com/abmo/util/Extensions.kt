package com.abmo.util

import com.abmo.common.Logger
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileNotFoundException

fun Any.toJson(): String = Gson().toJson(this)


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

fun isValidPath(filePath: String?): Boolean {
    filePath?.let { path ->
        return try {
            val outputFile = File(path).canonicalFile
            val outputDir = outputFile.parentFile

            if (path.endsWith("/") || path.endsWith("\\")) {
                Logger.error("Invalid File path.")
                return false
            }

            if (outputDir != null) {
                if (!outputDir.exists()) {
                    Logger.error("Output directory does not exist: ${outputDir.absolutePath}")
                    return false
                }
                if (!outputDir.isDirectory) {
                    Logger.error("Output path is not a directory: ${outputDir.absolutePath}")
                    return false
                }
            }

            if (outputFile.name.isBlank()) {
                Logger.error("No valid file name specified.")
                return false
            }
            if (!outputFile.name.endsWith(".mp4", ignoreCase = true)) {
                // probably mp4 isn't the only media type the site uses
                Logger.error("File must have a .mp4 extension: ${outputFile.name}")
                return false
            }
            if (outputFile.exists()) {
                Logger.error("File already exists: ${outputFile.absolutePath}")
                return false
            }

            true // all checks passed
        } catch (e: FileNotFoundException) {
            Logger.error("Unable to access path: ${e.message}")
            false
        } catch (e: SecurityException) {
            Logger.error("Error: Access denied to path: ${path}. ${e.message}")
            false
        }
    }
    return true
}
