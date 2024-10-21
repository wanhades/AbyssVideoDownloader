package com.abmo

import com.abmo.model.Config
import com.abmo.util.CryptoHelper
import com.abmo.util.displayProgressBar
import com.mashape.unirest.http.Unirest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import model.Range
import model.SimpleVideo
import model.Video
import model.toSimpleVideo
import util.toJson


class ScraperService(
    private val cryptoHelper: CryptoHelper
) {

    suspend fun downloadVideo(config: Config) {
        var totalBytesDownloaded = 0L
        val startTime = System.currentTimeMillis()
        val video = getVideoMetaData(config.url, config.header)
        val simpleVideo = video?.toSimpleVideo(config.resolution)
        val segmentBodies = generateSegmentsBody(simpleVideo)
        val segmentUrl = getSegmentUrl(video)
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
        println("\nDownload complete")
    }


    fun getVideoMetaData(url: String, headers: Map<String, String>?): Video? {
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