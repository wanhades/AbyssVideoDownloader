package com.abmo.providers

import com.abmo.util.fetchDocument
import java.nio.charset.Charset

class SieutamphimProvider: Provider {

    override fun getVideoID(url: String): String? {
        val splits = url.split("--")
        val originalUrl = splits[0]
        val episodeInput = (splits.getOrNull(1)?.toIntOrNull()?.minus(1)) ?: 0
        val document = originalUrl.fetchDocument()
        val episodes = document.select("div#mytick span.server-hx").nextAll()
            .select("button span").ifEmpty { return null }

        if (episodeInput >= episodes.size) return null

        val encryptedUrl = episodes[episodeInput].attr("data-src")
        val regex = """const\s+key\s*=\s*(\d+);""".toRegex()
        val key = regex.find(document.html())?.groupValues?.getOrNull(1)
            ?.toIntOrNull() ?: return null
        val videoID = decodeXor(encryptedUrl, key).substringAfterLast("/")

        return videoID
    }

    private fun decodeXor(encodedStr: String, key: Int): String {
        val xorEncodedBytes = encodedStr.map { it.code.toByte() }.toByteArray()
        val decodedBytes = xorEncryptDecrypt(xorEncodedBytes, key)
        return String(decodedBytes, Charset.defaultCharset())
    }


    private fun xorEncryptDecrypt(data: ByteArray, key: Int): ByteArray {
        return data.map { it.toInt() xor key }.map { it.toByte() }.toByteArray()
    }

}