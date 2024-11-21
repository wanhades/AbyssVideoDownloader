package com.abmo.services

import com.abmo.common.Logger
import com.abmo.crypto.CryptoHelper
import com.abmo.model.*
import com.abmo.util.displayProgressBar
import com.abmo.util.toJson
import com.abmo.util.toReadableTime
import com.mashape.unirest.http.Unirest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class VideoDownloader: KoinComponent {

    private val cryptoHelper: CryptoHelper by inject()

    /**
     * Downloads video segments in parallel, decrypts the header of each segment, and merges them into a single MP4 file.
     * This function uses coroutines for concurrent downloading with a limit on the number of concurrent downloads.
     * The header of each segment is decrypted only once (on the first chunk) using `isHeader` to distinguish it.
     *
     * @param config The configuration containing settings like output file path and connection limits, resolution.
     * @param videoMetadata The metadata of the video, used to generate segment data and the decryption key.
     * @throws Exception If there are errors during the download or file operations.
     */
    suspend fun downloadSegmentsInParallel(config: Config, videoMetadata: Video?) {
        val simpleVideo = videoMetadata?.toSimpleVideo(config.resolution)
        val segmentBodies = generateSegmentsBody(simpleVideo)
        val segmentUrl = getSegmentUrl(videoMetadata)
        val decryptionKey = cryptoHelper.getKey(simpleVideo?.size)


        val tempDir = initializeDownloadTempDir(config, simpleVideo, segmentBodies.size)

        val segmentsToDownload = segmentBodies.filter { (index, _) -> index in tempDir.second }.ifEmpty {
            segmentBodies
        }

        // reference: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-semaphore/
        // used to limit the number of concurrent coroutines executing the download tasks.
        val semaphore = Semaphore(config.connections)
        val totalSegments = segmentsToDownload.size
        val mediaSize = segmentsToDownload.size * 2097152L
        val downloadedSegments = AtomicInteger(0)
        val totalBytesDownloaded = AtomicLong(0L)

        val startTime = System.currentTimeMillis()

        coroutineScope {
            val downloadJobs = segmentsToDownload.entries.mapIndexed { _, segmentBody ->
                async(Dispatchers.IO) {
                    val index = segmentBody.key
                    semaphore.withPermit {
                        var isHeader = true
                        requestSegment(segmentUrl, segmentBody.value.toJson(), index).collect { chunk ->
                            val array = if (isHeader) {
                                isHeader = false
                                cryptoHelper.decryptAESCTR(chunk, decryptionKey)
                            } else {
                                chunk
                            }
                            File(tempDir.first.name, "segment_$index").appendBytes(array)
                            totalBytesDownloaded.addAndGet(array.size.toLong())
                        }
                    }
                    downloadedSegments.incrementAndGet()

                }
            }

            val progressJob = launch {
                var lastUpdateTime = System.currentTimeMillis()
                while (isActive) {
                    lastUpdateTime = displayProgressBar(
                        mediaSize,
                        totalSegments,
                        totalBytesDownloaded.toLong(),
                        downloadedSegments.get(),
                        startTime,
                        lastUpdateTime
                    )
                    delay(1000)
                }
            }
            downloadJobs.awaitAll()
            progressJob.cancel()
        }
        println("\n")
        Logger.debug("All segments have been downloaded successfully!")
        Logger.info("merging segments into mp4 file...")
        config.outputFile?.let { mergeSegmentsIntoMp4File(tempDir.first, it) }

    }


    @Deprecated("Use downloadSegmentsInParallel instead.",
        ReplaceWith("downloadSegmentsInParallel(config, videoMetadata)"))
    suspend fun downloadVideo(config: Config, videoMetadata: Video?) {
        var totalBytesDownloaded = 0L
        val startTime = System.currentTimeMillis()
        val simpleVideo = videoMetadata?.toSimpleVideo(config.resolution)
        val segmentBodies = generateSegmentsBody(simpleVideo)
        val segmentUrl = getSegmentUrl(videoMetadata)
        val decryptionKey = cryptoHelper.getKey(simpleVideo?.size)

        for (body in segmentBodies) {
            var isFirst = true
            requestSegment(segmentUrl, body.toJson()).collect { chunk ->
                val array = if (isFirst) {
                    isFirst = false
                    cryptoHelper.decryptAESCTR(chunk, decryptionKey)
                } else {
                    chunk
                }
                config.outputFile?.appendBytes(array)
                totalBytesDownloaded += array.size
                displayProgressBar(totalBytesDownloaded, simpleVideo?.size!!.toLong(), startTime)
            }
        }
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime).toReadableTime()
        println("\nDownload took: $duration")
        println("\nDownload complete")
    }



    private fun mergeSegmentsIntoMp4File(segmentFolderPath: File, output: File) {
        val segmentFiles  = segmentFolderPath.listFiles { file -> file.name.startsWith("segment_") }
            ?.toList()
            ?.sortedBy { it.name.removePrefix("segment_").toIntOrNull() }
            ?: emptyList()
        segmentFiles.forEach {
            output.appendBytes(it.readBytes())
        }

        Logger.success("Segments merged successfully.")

        if (segmentFolderPath.exists() && segmentFolderPath.isDirectory) {
            val files = segmentFolderPath.listFiles()

            if (files != null) {
                for (file in files) {
                    file.delete()
                }
            }

            if (!segmentFolderPath.delete()) {
                Logger.error("Failed to delete folder: ${segmentFolderPath.absolutePath}")
//                Logger.info("Deleted temporary folder at: ${segmentFolderPath.absolutePath}")
            }
        } else {
            Logger.error("Folder does not exist or is not a directory: ${segmentFolderPath.absolutePath}")
        }
    }


    /**
     * Sends an HTTP GET request to retrieve and decode video metadata.
     *
     * @param url The URL to send the GET request to.
     * @param headers A map of headers to include in the request.
     * @return The decoded video metadata, or null if the extraction or decoding fails.
     */
    fun getVideoMetaData(url: String, headers: Map<String, String?>?): Video? {
        Logger.debug("Starting HTTP GET request to $url")
        val response = Unirest.get(url)
            .headers(headers)
            .asString()

        val encryptedData = response.body
        val responseCode = response.status
        Logger.debug("Received response with status $responseCode", responseCode !in 200..299)

        val encryptedVideoData = extractEncryptedVideoMetaData(encryptedData)

        return cryptoHelper.decodeEncryptedString(encryptedVideoData)
    }


    private fun extractEncryptedVideoMetaData(html: String): String? {
        Logger.debug("Starting extraction of encrypted video metadata from HTML content.")
        val regex = """JSON\.parse\(atob\("([^"]+)"\)\)""".toRegex()
        val matchResult = regex.find(html)

        val result = matchResult?.groups?.get(1)?.value

        if (matchResult != null) {
            Logger.debug("Encrypted video metadata extracted successfully.")
            Logger.debug("Encrypted metadata (truncated): ${result?.take(100)}...")
        } else {
            Logger.debug("No encrypted video metadata found in the provided HTML.", true)
        }

        return matchResult?.groups?.get(1)?.value
    }

    private fun getSegmentUrl(video: Video?): String {
        return "https://${video?.domain}/${video?.id}"
    }


    private fun initializeDownloadTempDir(
        config: Config,
        simpleVideo: SimpleVideo?,
        totalSegments: Int
    ): Pair<File, List<Int>> {
        val tempFolderName = "temp_${simpleVideo?.slug}_${simpleVideo?.label}"
        // no need to check if path exists before creating temp folder we already did that in Main.kt
        val tempFolder = File(config.outputFile?.parentFile, tempFolderName)

        if (tempFolder.exists() && tempFolder.isDirectory) {
            Logger.info("Resuming download from temporary folder: $tempFolderName. Continuing from previously downloaded segments.")
            println("\n")
            val existingSegments = tempFolder.listFiles { file ->
                if (file.isFile && file.length() < 2097152) {
                    file.delete()
                }
                file.isFile && file.name.matches(Regex("segment_\\d+"))
            }?.mapNotNull { file ->
                file.name.removePrefix("segment_").toIntOrNull()
            }?.toSet() ?: emptySet()

            val allSegmentNames = (0 until totalSegments).toList()

            val missingSegmentNames = allSegmentNames.filterNot { it in existingSegments }

            return tempFolder to missingSegmentNames
        } else {
            Logger.info("Creating temporary folder $tempFolderName")
            println("\n")
            tempFolder.mkdirs()
        }
        return tempFolder to emptyList()
    }

    private fun generateRanges(size: Long, step: Long = 2097152): List<LongRange> {
        val ranges = mutableListOf<LongRange>()

        // if the size is less than or equal to step size return a single range
        if (size <= step) {
            ranges.add(0 until size)
            return ranges
        }

        var start = 0L
        while (start < size) {
            val end = minOf(start + step, size) // ensure the end doesn't exceed the size
            ranges.add(start until end)
            start = end
        }

        return ranges
    }


    private fun generateSegmentsBody(simpleVideo: SimpleVideo?): Map<Int, String> {
        Logger.debug("Generating segment POST request body and encrypting the data.")
        val fragmentList = mutableMapOf<Int, String>()
        val encryptionKey = cryptoHelper.getKey(simpleVideo?.slug)
        if (simpleVideo?.size != null) {
            val ranges = generateRanges(simpleVideo.size)
            ranges.forEachIndexed { index, range ->
                val body = simpleVideo.copy(
                    range = Range(range.first, range.last)
                )
                val encryptedBody = cryptoHelper.encryptAESCTR(body.toJson(), encryptionKey)
                fragmentList[index] = encryptedBody
            }
            Logger.debug("${fragmentList.size} request body generated")
            return fragmentList
        }
        return emptyMap()
    }


    private suspend fun requestSegment(url: String, body: String, index: Int? = null): Flow<ByteArray> = flow {
//        println("\n")
        Logger.debug("[$index] Starting HTTP POST request to $url with body length: ${body.length}. Body (truncated): \"...${body.takeLast(30)}")
        val response = Unirest.post(url)
            .header("Content-Type", "application/json")
            .body("""{"hash":$body}""")
            .asBinary()

        val rawBody = response.rawBody
        val responseCode = response.status

//        println("\n")
        Logger.debug("[$index] Received response with status $responseCode\n", responseCode !in 200..299)

        val buffer = ByteArray(65536)
        var bytesRead: Int

        rawBody.use { stream ->
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                emit(buffer.copyOf(bytesRead))
            }
        }
    }.flowOn(Dispatchers.IO)


}