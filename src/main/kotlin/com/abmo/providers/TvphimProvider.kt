package com.abmo.providers

import com.abmo.util.JavaScriptExecutor
import com.abmo.util.fetchDocument


class TvphimProvider(
    private val javaScriptExecutor: JavaScriptExecutor
): Provider {

    override fun getVideoID(url: String): String? {
        val hProUrl = url.fetchDocument()
            .select("a[title=\"Server H.PRO\"]").attr("href")
        if (hProUrl.isNullOrBlank()) return null

        val document = hProUrl.fetchDocument()
        val jsCode = document.select("div[class=absolute inset-0]")
            .select("script").html()

        val videoID = javaScriptExecutor.runJavaScriptCode(
            "tvphim.js",
            "extractVideoID",
            jsCode)

        return videoID
    }

}