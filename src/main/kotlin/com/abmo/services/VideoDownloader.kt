package com.abmo.services

import com.abmo.model.*
import com.abmo.util.CryptoHelper
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
import java.io.File

class VideoDownloader(
    private val cryptoHelper: CryptoHelper
) {

    suspend fun downloadSegmentsInParallel(config: Config, videoMetadata: Video?) {
        val simpleVideo = videoMetadata?.toSimpleVideo(config.resolution)
        val segmentBodies = generateSegmentsBody(simpleVideo)
        val segmentUrl = getSegmentUrl(videoMetadata)
        val decryptionKey = cryptoHelper.getKey(simpleVideo?.size)

        val tempFolder = File(config.outputFile?.parentFile, "temp_${simpleVideo?.slug}_${simpleVideo?.label}")
        // no need to check if path exists before creating temp folder we already did that in Main.kt (why do I use WE I'M ALONE AT THIS)
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
                        var isFirst = true
                        requestSegment(segmentUrl, segmentBody.toJson()).collect { chunk ->
                            val array = if (isFirst) {
                                isFirst = false
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
        println("\n\nDownload took: $duration")
        println("\nAll segments have been downloaded successfully!")
        println("merging segments into mp4 file...")
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


    private fun mergeSegmentsIntoMp4(segmentFolderPath: File, output: File) {
        val segmentFiles  = segmentFolderPath.listFiles { file -> file.name.startsWith("segment_") }
            ?.toList()
            ?.sortedBy { it.name.removePrefix("segment_").toIntOrNull() }
            ?: emptyList()
        segmentFiles.forEach {
            output.appendBytes(it.readBytes())
        }

        println("merge complete successfully")

        if (segmentFolderPath.exists() && segmentFolderPath.isDirectory) {
            val files = segmentFolderPath.listFiles()

            if (files != null) {
                for (file in files) {
                    file.delete()
                }
            }

            if (segmentFolderPath.delete()) {
                println("Deleted temporary folder at: ${segmentFolderPath.absolutePath}")
            } else {
                println("Failed to delete folder: ${segmentFolderPath.absolutePath}")
            }
        } else {
            println("Folder does not exist or is not a directory: ${segmentFolderPath.absolutePath}")
        }
    }


    fun getVideoMetaData(url: String, headers: Map<String, String?>?): Video? {
        val document = Unirest.get(url)
            .headers(headers)
            .asString().body
        val encryptedVideoData = extractEncryptedVideoMetaData(document)
        return cryptoHelper.decodeEncryptedString(encryptedVideoData)
    }


    private fun extractEncryptedVideoMetaData(html: String): String? {
        val regex = """JSON\.parse\(atob\("([^"]+)"\)\)""".toRegex()
        val matchResult = regex.find(html)
        return matchResult?.groups?.get(1)?.value
    }

    private fun getSegmentUrl(video: Video?): String {
        return "https://${video?.domain}/${video?.id}"
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


    private fun generateSegmentsBody(simpleVideo: SimpleVideo?): List<String> {
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
            return fragmentList
        }
        return emptyList()
    }


    private suspend fun requestSegment(url: String, body: String): Flow<ByteArray> = flow {
        val response = Unirest.post(url)
            .header("Content-Type", "application/json")
            .body("""{"hash":$body}""")
            .asBinary().rawBody

        val buffer = ByteArray(65536)
        var bytesRead: Int

        response.use { stream ->
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                emit(buffer.copyOf(bytesRead))
            }
        }
    }.flowOn(Dispatchers.IO)


}