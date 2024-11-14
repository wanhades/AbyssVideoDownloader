package com.abmo.util

/**
 * Converts a duration in milliseconds to a human-readable time format.
 *
 * @return A String representing the duration in hours, minutes, and seconds.
 */
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

/**
 * Converts a byte value to a human-readable format (KB, MB, GB).
 *
 * @return A String representing the size in a human-readable format, or an empty String if null.
 */
fun Long?.formatBytes(): String {
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