package com.abmo.services

import com.abmo.common.Logger
import com.abmo.crypto.CryptoHelper
import com.abmo.model.*
import com.abmo.util.displayProgressBar
import com.abmo.util.toJson
import com.abmo.util.toReadableTime
import com.mashape.unirest.http.Unirest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

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

        val tempFolderName = "temp_${simpleVideo?.slug}_${simpleVideo?.label}"
        val tempFolder = File(config.outputFile?.parentFile, tempFolderName)
        // no need to check if path exists before creating temp folder we already did that in Main.kt
        Logger.info("Creating temporary folder $tempFolderName")
        tempFolder.mkdir()


        // reference: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-semaphore/
        // used to limit the number of concurrent coroutines executing the download tasks.
        val semaphore = Semaphore(config.connections)
        val totalSegments = segmentBodies.size
        var downloadedSegments = 0
        var totalBytesDownloaded = 0L

        val startTime = System.currentTimeMillis()

        coroutineScope {
            val downloadJobs = segmentBodies.mapIndexed { i, segmentBody ->
                async {
                    semaphore.withPermit {
                        var isHeader = true
                        requestSegment(segmentUrl, segmentBody.toJson(), i).collect { chunk ->
                            val array = if (isHeader) {
                                isHeader = false
                                cryptoHelper.decryptAESCTR(chunk, decryptionKey)
                            } else {
                                chunk
                            }
                            File(tempFolder, "segment_$i").appendBytes(array)
                            totalBytesDownloaded += array.size
                        }
                    }
                    downloadedSegments += 1
                    displayProgressBar(i + 1, totalSegments, totalBytesDownloaded, downloadedSegments, startTime)
                }
            }
            downloadJobs.awaitAll()
        }
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime).toReadableTime()
        println("\n")
        Logger.info("Download took: $duration")
        Logger.success("All segments have been downloaded successfully!")
        Logger.info("merging segments into mp4 file...")
        config.outputFile?.let { mergeSegmentsIntoMp4(tempFolder, it) }

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


    /**
     * Merges video segments into a single MP4 file and cleans up the temporary segment folder.
     *
     * @param segmentFolderPath The folder containing the video segments.
     * @param output The output file where the merged segments will be written.
     * @throws Exception If there is an error reading or writing the segment files.
     */
    private fun mergeSegmentsIntoMp4(segmentFolderPath: File, output: File) {
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

            if (segmentFolderPath.delete()) {
                Logger.info("Deleted temporary folder at: ${segmentFolderPath.absolutePath}")
            } else {
                Logger.error("Failed to delete folder: ${segmentFolderPath.absolutePath}")
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

    /**
     * Extracts encrypted video metadata from an HTML string using a regex pattern.
     *
     * @param html The HTML content to extract the metadata from.
     * @return The extracted encrypted video metadata, or null if not found.
     */
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

    /**
     * Generates a list of `LongRange` objects for splitting a given size into smaller ranges.
     *
     * @param size The total size to split into ranges.
     * @param step The size of each range (default is 2MB).
     * @return A list of `LongRange` representing the size ranges.
     */
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

    /**
     * Generates and encrypts the body for segment POST requests based on video size.
     *
     * @param simpleVideo The video data to generate segments for.
     * @return A list of encrypted segment bodies as strings or emptyList if video is size is null.
     */
    private fun generateSegmentsBody(simpleVideo: SimpleVideo?): List<String> {
        Logger.debug("Generating segment POST request body and encrypting the data.")
        val fragmentList = mutableListOf<String>()
        val encryptionKey = cryptoHelper.getKey(simpleVideo?.slug)
        if (simpleVideo?.size != null) {
            val ranges = generateRanges(simpleVideo.size)
            ranges.forEach { range ->
                val body = simpleVideo.copy(
                    range = Range(range.first, range.last)
                )
                val encryptedBody = cryptoHelper.encryptAESCTR(body.toJson(), encryptionKey)
                fragmentList.add(encryptedBody)
            }
            Logger.debug("${fragmentList.size} request body generated")
            return fragmentList
        }
        return emptyList()
    }


    /**
     * Sends an HTTP POST request and returns the response body as a flow of byte arrays.
     *
     * @param url The URL to send the POST request to.
     * @param body The body of the POST request.
     * @param index An optional index for logging purposes.
     * @return A flow of byte arrays representing the response body.
     * @throws Exception If the request fails or the response status is not successful.
     */
    private suspend fun requestSegment(url: String, body: String, index: Int? = null): Flow<ByteArray> = flow {
//        println("\n")
        Logger.debug("[$index] Starting HTTP POST request to $url with body length: ${body.length}. Body (truncated): \"...$body")
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