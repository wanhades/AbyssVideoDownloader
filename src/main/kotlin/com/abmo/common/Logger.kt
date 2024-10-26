package com.abmo.common

object Logger {
    init {
        enableAnsiOnWindows()
    }

    const val RESET = "\u001B[0m"
    private const val PURPLE = "\u001B[35m"
    private const val YELLOW = "\u001B[33m"
    private const val RED = "\u001B[31m"
    private const val CYAN = "\u001B[36m"
    private const val GREEN = "\u001B[32m"

    // Enable ANSI support on Windows CMD (for Windows 10+)
    private fun enableAnsiOnWindows() {
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                val console = System.console()
                if (console != null) {
                    ProcessBuilder("cmd", "/c", "echo", "\u001B[31m").inheritIO().start().waitFor()
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private fun colorize( text: String, colorCode: String): String {
        return "$colorCode$text$RESET"
    }

    fun info(message: String) {
        println(colorize("INFO: $message", CYAN))
    }

    fun warn(message: String) {
        println(colorize("WARN: $message", YELLOW))
    }

    fun error(message: String) {
        println(colorize("ERROR: $message", RED))
    }

    fun debug(message: String, isError: Boolean = false) {
        if (Constants.VERBOSE) {
            val debugTextColor = if (isError) { RED } else { PURPLE }
            println(colorize("DEBUG: $message", debugTextColor))
        }
    }

    fun success(message: String) {
        println(colorize("SUCCESS: $message", GREEN))
    }
}