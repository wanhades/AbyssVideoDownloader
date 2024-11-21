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
 * @param mediaSize The size of the current media being processed.
 * @param totalSegments The total number of segments to be processed.
 * @param bytesDownloaded The total number of bytes downloaded.
 * @param totalDownloaded The total number of segments downloaded so far.
 * @param startTime The start time of the download in milliseconds.
 */
fun displayProgressBar(
    mediaSize: Long?,
    totalSegments: Int,
    bytesDownloaded: Long,
    totalDownloaded: Int,
    startTime: Long,
    lastUpdateTime: Long
): Long {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastUpdateTime < 1000) {
        return lastUpdateTime
    }

    val progress = totalDownloaded.toDouble() / totalSegments
    val barLength = 50
    val filledLength = (progress * barLength).toInt()
    val bar = "█".repeat(filledLength) + " ".repeat(barLength - filledLength)

    val elapsedTime = (currentTime - startTime) / 1000
    val downloadSpeed = if (elapsedTime > 0) (bytesDownloaded / elapsedTime) else 0
    val formattedSpeed = downloadSpeed.formatAsReadableSize() + "/s"

    val remainingTime = if (downloadSpeed > 0) {
        ((mediaSize ?: 0) - bytesDownloaded) / downloadSpeed
    } else {
        0
    }.toReadableTime()

    val elapsed = elapsedTime.toReadableTime()

    print(
        "\rDownloading ${(progress * 100).toInt()}% |$bar| " +
                "(${bytesDownloaded.formatBytes().toInt()}/${mediaSize?.formatAsReadableSize()}, $formattedSpeed) " +
                "[${elapsed} / ${remainingTime}] "
    )

    if (progress >= 1.0) {
        println()
    }

    return currentTime
}
