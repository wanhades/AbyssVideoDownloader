package com.abmo

import com.abmo.model.Config
import com.abmo.util.CryptoHelper
import com.abmo.util.JavaScriptRunner
import util.formatBytes
import util.getVideoID
import util.isValidPath
import java.io.File
import kotlin.system.exitProcess


suspend fun main(args: Array<String>) {
    val javaScriptRunner = JavaScriptRunner()
    val cryptoHelper = CryptoHelper(javaScriptRunner)
    val scraperService = ScraperService(cryptoHelper)


    val outputFileName = if (args.isNotEmpty() && args[0] == "-o") {
        args.getOrNull(1)
    } else {
        null
    }

    if (outputFileName != null && !isValidPath(outputFileName)) {
        exitProcess(0)
    }

    val scanner = java.util.Scanner(System.`in`)


    try {
        println("Enter the video URL or ID (e.g., K8R6OOjS7):")
        val videoID = scanner.nextLine().getVideoID()

        val url = "https://abysscdn.com/?v=$videoID"
        val videoSources = scraperService.getVideoMetaData(url)?.sources
            ?.sortedBy { it?.label?.filter { char -> char.isDigit() }?.toInt() }

        if (videoSources == null) {
            println("video with ID $videoID not found")
            exitProcess(0)
        }

        println("Choose the resolution you want to download:")
        videoSources
            .forEachIndexed { index, video ->
                println("${index + 1}] ${video?.label} - ${formatBytes(video?.size?.toLong())}")
            }

        val choice = scanner.nextInt()
        val resolution = videoSources[choice - 1]?.label


        if (resolution != null) {

            val config = if (outputFileName == null) {
                println("\nOutput file not specified. The video will be saved in the 'Downloads' folder as '${url.getVideoID()}_$resolution.mp4'.")
                Config(url, resolution)
            } else {
                Config(url, resolution, File(outputFileName))
            }
            println("\nvideo with id $videoID and resolution $resolution being processed....")
            scraperService.downloadVideo(config)
        }
    } catch (e: NoSuchElementException) {
        println("\nCtrl + C detected. Exiting...")
    }


}
