package com.abmo.util

import com.abmo.common.Constants.DEFAULT_CONCURRENT_DOWNLOAD_LIMIT
import com.abmo.common.Logger

/**
 * A class for parsing command-line arguments.
 *
 * @param args The array of command-line arguments.
 */
class CliArguments(private val args: Array<String>) {

    /**
     * Extracts headers from command-line arguments in the format "--header key:value".
     *
     * @return A map of header names to their values, or null if no headers are found.
     */
    fun getHeaders(): Map<String, String>? {
        val headers = mutableMapOf<String, String>()

        for (i in args.indices) {
            if (args[i] in arrayOf("--header", "-H") && i + 1 < args.size) {
                val (key, value) = args[i + 1].split(":", limit = 2).map { it.trim() }
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    headers[key] = value
                } else {
                    Logger.error("Invalid header format. Use 'Header-Name: Header-Value'")
                }
            }
        }

        return headers.ifEmpty { null }
    }

    /**
     * Retrieves the output file name from command-line arguments.
     *
     * @return The output file path as a String, or null if not specified.
     */
    fun getOutputFileName(): String? {
        val index = args.indexOf("-o")
        if (index != -1 && index + 1 < args.size) {
            val filePath = args[index + 1]
            return  filePath
        }
        return null
    }

    /**
     * Retrieves the number of parallel connections from command-line arguments.
     *
     * @return The number of connections, constrained between 1 and 10.
     *         Returns the default value if not specified.
     */
    fun getParallelConnections(): Int {
        val maxConnections = 10
        val minConnections = 1
        val connectionArgIndex = args.indexOfFirst { it == "--connections" || it == "-c"}
        if (connectionArgIndex != -1 && connectionArgIndex + 1 < args.size) {
            val connectionValue = args[connectionArgIndex + 1].toIntOrNull()
            if (connectionValue != null) {
                return connectionValue.coerceIn(minConnections, maxConnections)
            }
        }

        return DEFAULT_CONCURRENT_DOWNLOAD_LIMIT
    }

    /**
     * Checks if the verbose flag is enabled in the command-line arguments.
     *
     * @return true if "--verbose" is present, false otherwise.
     */
    fun isVerboseEnabled() = args.contains("--verbose")

}