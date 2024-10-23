package com.abmo.util


fun displayProgressBar(bytesDownloaded: Long, totalSize: Long, startTime: Long) {
    val progress = (bytesDownloaded.toDouble() / totalSize.toDouble()) * 100
    val barLength = 50
    val filledLength = (progress / 100 * barLength).toInt()
    val bar = "=".repeat(filledLength) + "-".repeat(barLength - filledLength)
    val formattedProgress = String.format("%.2f", progress)
    val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
    val downloadSpeed = if (elapsedTime > 0) (bytesDownloaded / elapsedTime) else 0.0
    val formattedSpeed = String.format("%.2f KB/s", downloadSpeed / 1024.0)

    print("\rProgress: |$bar| $formattedProgress% - Speed: $formattedSpeed")
    if (progress >= 100) {
        println()
    }
}
