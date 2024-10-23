package com.abmo.providers

import com.abmo.util.fetchDocument


class TvhaywProvider: Provider {

    // TODO: Refactor duplicated code similar to PhimbetProvider.
    // The logic for extracting video URLs is repeated across multiple providers.
    // Consider moving the common code into a shared base class or utility function to reduce duplication.
    override fun getVideoID(url: String): String? {
        val regex = """https://short\.ink/([a-zA-Z0-9_\-@.]+)""".toRegex()
        val document = url.fetchDocument()
            .getElementById("ploption")?.select("a")
        val id = document?.find { it.html().contains("H.PRO") }
            ?.attr("data-linkid") ?: return null

        val embedPlayer = "https://tvhayw.org/play?id=$id".fetchDocument().html()
        val videoID = regex.find(embedPlayer)?.value?.substringAfterLast("/")

        return videoID
    }
}