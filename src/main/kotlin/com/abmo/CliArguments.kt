package com.abmo

import com.abmo.Constant.DEFAULT_CONCURRENT_DOWNLOAD_LIMIT

class CliArguments(private val args: Array<String>) {

    fun getHeaders(): Map<String, String>? {
        val headers = mutableMapOf<String, String>()
        var i = 0

        while (i < args.size) {
            when (args[i]) {
                "--header", "-H" -> {
                    if (i + 1 < args.size) {
                        val header = args[i + 1]
                        val parts = header.split(":", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()
                            headers[key] = value
                        } else {
                            println("Invalid header format. Use 'Header-Name: Header-Value'")
                        }
                        i += 1
                    }
                }
            }
            i += 1
        }

        return headers.ifEmpty { null }
    }

    fun getOutputFileName(args: Array<String>): String? {
        val index = args.indexOf("-o")
        if (index != -1 && index + 1 < args.size) {
            val filePath = args[index + 1]
            return  filePath
        }
        return null
    }

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

}