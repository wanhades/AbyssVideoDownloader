package com.abmo.providers

import com.abmo.util.JavaScriptExecutor
import org.jsoup.Jsoup
import util.fetchDocument


class TvphimProvider(
    private val javaScriptExecutor: JavaScriptExecutor
): Provider {

    override fun getVideoID(url: String): String? {
        val hProUrl = url.fetchDocument()
            .select("a[title=\"Server H.PRO\"]").attr("href")
        if (hProUrl.isNullOrBlank()) return null

        val document = Jsoup.connect(hProUrl).get()
        val jsCode = document.select("div[class=absolute inset-0]")
            .select("script").html()

        val videoID = javaScriptExecutor.runJavaScriptCode(
            "tvphim.js",
            "extractVideoID",
            jsCode)

        return videoID
    }

}