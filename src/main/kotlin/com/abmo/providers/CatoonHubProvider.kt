package com.abmo.providers

import com.abmo.util.fetchDocument

class CatoonHubProvider: Provider {
    override fun getVideoID(url: String): String? {
        return url.fetchDocument().select("div.aspect-video.overflow-hidden.border-y iframe")
            .attr("src").substringAfterLast("/").ifEmpty { null }
    }
}