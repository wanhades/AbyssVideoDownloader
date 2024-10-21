package com.abmo

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

        return headers
    }

    fun getOutputFileName(args: Array<String>): String? {
        for (i in args.indices) {
            if (args[i] == "-o" && i + 1 < args.size) {
                return args[i + 1]
            }
        }
        return null
    }

}