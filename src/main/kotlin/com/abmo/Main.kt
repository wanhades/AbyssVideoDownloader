package com.abmo

import com.abmo.common.Constants
import com.abmo.common.Logger
import com.abmo.model.Config
import com.abmo.services.ProviderDispatcher
import com.abmo.services.VideoDownloader
import com.abmo.crypto.CryptoHelper
import com.abmo.executor.JavaScriptExecutor
import com.abmo.util.*
import java.io.File
import kotlin.system.exitProcess


suspend fun main(args: Array<String>) {

    val javaScriptExecutor = JavaScriptExecutor()
    val cryptoHelper = CryptoHelper(javaScriptExecutor)
    val videoDownloader = VideoDownloader(cryptoHelper)
    val cliArguments = CliArguments(args)
    val providerDispatcher = ProviderDispatcher(javaScriptExecutor)


    val outputFileName = cliArguments.getOutputFileName(args)
    val headers = cliArguments.getHeaders()
    val numberOfConnections = cliArguments.getParallelConnections()
    Constants.VERBOSE = args.contains("--verbose")

    if (outputFileName != null && !isValidPath(outputFileName)) {
        exitProcess(0)
    }

    val scanner = java.util.Scanner(System.`in`)


    try {
        println("Enter the video URL or ID (e.g., K8R6OOjS7):")
        val videoUrl = scanner.nextLine()

        val dispatcher = providerDispatcher.getProviderForUrl(videoUrl)
        val videoID = dispatcher.getVideoID(videoUrl)

        val defaultHeader = if (videoUrl.isValidUrl()) {
            mapOf("Referer" to videoUrl?.extractReferer())
        } else { emptyMap() }

        val url = "https://abysscdn.com/?v=$videoID"
        val videoMetadata = videoDownloader.getVideoMetaData(url, headers ?: defaultHeader)

        val videoSources = videoMetadata?.sources
            ?.sortedBy { it?.label?.filter { char -> char.isDigit() }?.toIntOrNull() }

        if (videoSources == null) {
            Logger.error("Video with ID $videoID not found")
            exitProcess(0)
        }

        // For some reason ANSI applies to rest of text in the terminal starting from here
        // I'm not sure what causes that, so I removed all error logger here, and it still occurs
        // the only solution for now is to reset ANSI before displaying the message
        println("${Logger.RESET}Choose the resolution you want to download:")
        videoSources
            .forEachIndexed { index, video ->
                println("${index + 1}] ${video?.label} - ${formatBytes(video?.size)}")
            }

        val choice = scanner.nextInt()
        val resolution = videoSources[choice - 1]?.label


        if (resolution != null) {

            val defaultFileName = "${url.getParameter("v")}_${resolution}_${System.currentTimeMillis()}.mp4"
            val outputFile = outputFileName?.let { File(it) } ?: run {
                Logger.warn("No output file specified. The video will be saved to the current directory as '$defaultFileName'.\n")
                File(".", defaultFileName) // Default directory and name for saving video
            }

            val config = Config(url, resolution, outputFile, headers, numberOfConnections)
            Logger.info("video with id $videoID and resolution $resolution being processed...\n")
            videoDownloader.downloadSegmentsInParallel(config, videoMetadata)
        }
    } catch (e: NoSuchElementException) {
        println("\nCtrl + C detected. Exiting...")
    }


}
