package com.abmo.util

import com.abmo.common.Logger
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileNotFoundException

fun Any.toJson(): String = Gson().toJson(this)

/**
 * Fetches the HTML document from the URL represented by the string.
 *
 * @return The parsed `Document` object representing the HTML content.
 * @throws IllegalArgumentException if the URL is malformed or cannot be accessed.
 * @throws Exception if an I/O error occurs while attempting to retrieve the document.
 */
fun String.fetchDocument(): Document = Jsoup.connect(this).get()

/**
 * Parses the string as an HTML document using Jsoup.
 *
 * @return The parsed `Document` object representing the HTML content.
 */
fun String.toJsoupDocument(): Document = Jsoup.parse(this)

/**
 * Finds and returns the value associated with the given key in a JSON string.
 *
 * @param key The key to search for.
 * @return The corresponding value as a String, or null if not found.
 */
fun String.findValueByKey(key: String): String? {
    val regex = """"$key"\s*:\s*("[^"\\]*(?:\\.[^"\\]*)*"|[^\s,}]+)""".toRegex()
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
    if (filePath.isNullOrBlank()) return false

    return try {
        val outputFile = File(filePath).canonicalFile
        val outputDir = outputFile.parentFile

        // check for invalid file path
        if (filePath.endsWith("/") || filePath.endsWith("\\")) {
            Logger.error("Invalid File path.")
            return false
        }

        // check if the output directory exists and is a directory
        outputDir?.let {
            if (!it.exists()) {
                Logger.error("Output directory does not exist: ${it.absolutePath}")
                return false
            }
            if (!it.isDirectory) {
                Logger.error("Output path is not a directory: ${it.absolutePath}")
                return false
            }
        }

        // check if file name is valid
        if (outputFile.name.isBlank()) {
            Logger.error("No valid file name specified.")
            return false
        }

        // check if file extension is .mp4 (not about sure about restricting extension here but mostly source uses mp4)
        if (!outputFile.name.endsWith(".mp4", ignoreCase = true)) {
            Logger.error("File must have a .mp4 extension: ${outputFile.name}")
            return false
        }

        // check if file already exists
        if (outputFile.exists()) {
            Logger.error("File already exists: ${outputFile.absolutePath}")
            return false
        }

        true // all checks passed
    } catch (e: FileNotFoundException) {
        Logger.error("Unable to access path: ${e.message}")
        false
    } catch (e: SecurityException) {
        Logger.error("Error: Access denied to path: $filePath. ${e.message}")
        false
    }
}

