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


    private fun colorizeText(text: String, colorCode: String): String {
        return "$colorCode$text$RESET"
    }

    /**
     * Prints an informational message in cyan.
     *
     * @param message The message to be printed as an informational log.
     */
    fun info(message: String) {
        println(colorizeText("INFO: $message", CYAN))
    }

    /**
     * Prints a warning message in yellow.
     *
     * @param message The message to be printed as a warning log.
     */
    fun warn(message: String) {
        println(colorizeText("WARN: $message", YELLOW))
    }

    /**
     * Prints an error message in red.
     *
     * @param message The message to be printed as an error log.
     */
    fun error(message: String) {
        println(colorizeText("ERROR: $message", RED))
    }

    /**
     * Prints a debug message, color-coded based on whether it's an error or not, if verbose mode is enabled.
     *
     * @param message The message to be printed as a debug log.
     * @param isError Indicates whether the debug message represents an error (applies red color if true).
     */
    fun debug(message: String, isError: Boolean = false) {
        if (Constants.VERBOSE) {
            val debugTextColor = if (isError) { RED } else { PURPLE }
            println(colorizeText("DEBUG: $message", debugTextColor))
        }
    }

    /**
     * Prints a success message in green.
     *
     * @param message The message to be printed as a success log.
     */
    fun success(message: String) {
        println(colorizeText("SUCCESS: $message", GREEN))
    }
}