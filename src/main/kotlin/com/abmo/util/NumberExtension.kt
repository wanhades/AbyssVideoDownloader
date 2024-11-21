package com.abmo.util

/**
 * Converts a duration in milliseconds to a human-readable time format.
 *
 * @return A String representing the duration in hours, minutes, and seconds.
 */
fun Long.toReadableTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Converts a byte value to a human-readable format (KB, MB, GB).
 *
 * @return A String representing the size in a human-readable format, or an empty String if null.
 */
fun Long?.formatAsReadableSize(): String {
    if (this == null) return ""
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        this >= gigabyte -> String.format("%.2f GB", this / gigabyte)
        this >= megabyte -> String.format("%.2f MB", this / megabyte)
        this >= kilobyte -> String.format("%.2f KB", this / kilobyte)
        else -> "$this Bytes"
    }
}

fun Long.formatBytes(): Double {
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        this >= gigabyte -> this / gigabyte
        this >= megabyte -> this / megabyte
        this >= kilobyte -> this / kilobyte
        else -> this.toDouble()
    }
}