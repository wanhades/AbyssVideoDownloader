package com.abmo.util


fun displayProgressBar(bytesDownloaded: Long, totalSize: Long, startTime: Long) {
    val progress = (bytesDownloaded.toDouble() / totalSize.toDouble()) * 100
    val barLength = 50
    val filledLength = (progress / 100 * barLength).toInt()
    val bar = "█".repeat(filledLength) + " ".repeat(barLength - filledLength)
    val formattedProgress = String.format("%.2f", progress)
    val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
    val downloadSpeed = if (elapsedTime > 0) (bytesDownloaded / elapsedTime) else 0.0
    val formattedSpeed = String.format("%.2f KB/s", downloadSpeed / 1024.0)

    print("\rProgress: |$bar| $formattedProgress% - Speed: $formattedSpeed")
    if (progress >= 100) {
        println()
    }
}

/**
 * Displays a progress bar in the console for the download progress.
 *
 * @param current The index of the current segment being processed.
 * @param totalSegments The total number of segments to be processed.
 * @param bytesDownloaded The total number of bytes downloaded.
 * @param totalDownloaded The total number of segments downloaded so far.
 * @param startTime The start time of the download in milliseconds.
 */
fun displayProgressBar(current: Int, totalSegments: Int, bytesDownloaded: Long, totalDownloaded: Int, startTime: Long) {
    val progress = totalDownloaded.toDouble() / totalSegments
    val barLength = 50
    val filledLength = (progress * barLength).toInt()
    val bar = "█".repeat(filledLength) + " ".repeat(barLength - filledLength)

    val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
    val downloadSpeed = if (elapsedTime > 0) (bytesDownloaded / elapsedTime) else 0.0
    val formattedSpeed = String.format("%.2f KB/s", downloadSpeed / 1024.0)

    print("\rCurrent Segment $current |$bar| Segment $totalDownloaded/$totalSegments (${(progress * 100).toInt()}%) - Speed: $formattedSpeed")
    if (progress >= 100) {
        println()
    }
}
