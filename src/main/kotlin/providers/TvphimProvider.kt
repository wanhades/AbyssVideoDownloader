package com.abmo.providers

import com.abmo.util.JavaScriptRunner
import org.jsoup.Jsoup



class TvphimProvider(
    private val javaScriptRunner: JavaScriptRunner
): Provider {

    override fun getVideoID(url: String): String? {
        val hProUrl = Jsoup.connect(url).get()
            .select("a[title=\"Server H.PRO\"]").attr("href")
        if (hProUrl.isNullOrBlank()) return null

        val document = Jsoup.connect(hProUrl).get()
        val jsCode = document.select("div[class=absolute inset-0]")
            .select("script").html()

        val videoID = javaScriptRunner.runJavaScriptCode(
            "tvphim.js",
            "extractVideoID",
            jsCode)

        return videoID
    }

}