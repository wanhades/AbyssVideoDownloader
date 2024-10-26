package com.abmo.util

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