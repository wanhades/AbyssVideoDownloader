package com.abmo.providers

import com.abmo.util.fetchDocument

class FimmoiProvider: Provider {
    override fun getVideoID(url: String): String {
        return url.fetchDocument()
            .select("div.entry-content.clearfix p iframe")
            .attr("src")
            .substringAfterLast("/")
    }

}