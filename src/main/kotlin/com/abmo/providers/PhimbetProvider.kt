package com.abmo.providers

import com.abmo.util.fetchDocument
import com.abmo.util.findValueByKey


class PhimbetProvider: Provider {

    override fun getVideoID(url: String): String? {
        val regex = """https://short\.ink/([a-zA-Z0-9_\-@.]+)""".toRegex()
        val document = url.fetchDocument()
        val embedUrl = document.html().findValueByKey("embedUrl")
        val embedPage = embedUrl?.fetchDocument()?.html() ?: return null

        val videoID = regex.find(embedPage)?.value?.substringAfterLast("/")

        return videoID
    }
}