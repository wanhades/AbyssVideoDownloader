package com.abmo.providers

import com.abmo.executor.JavaScriptExecutor
import com.abmo.util.fetchDocument
import com.mashape.unirest.http.Unirest
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts


class TvphimProvider(
    private val javaScriptExecutor: JavaScriptExecutor
): Provider {

    override fun getVideoID(url: String): String? {
        configureTls13Protocol()
        val hProUrl = url.fetchDocument()
            .select("a[title=\"Server H.PRO\"]").attr("href")
        if (hProUrl.isNullOrBlank()) return null

        val document = hProUrl.fetchDocument()
        val jsCode = document.select("div[class=absolute inset-0]")
            .select("script").html()

        val videoID = javaScriptExecutor.runJavaScriptCode(
            javascriptFileName = "tvphim.js",
            identifier = "extractVideoID",
            arguments = arrayOf(jsCode)
        )

        return videoID
    }

    private fun configureTls13Protocol() {
        val sslContext = SSLContexts.custom()
            .useProtocol("TLSv1.3")
            .build()

        val tlsSocketFactory = SSLConnectionSocketFactory(sslContext)

        val httpClient = HttpClients.custom()
            .setSSLSocketFactory(tlsSocketFactory)
            .build()

        Unirest.setHttpClient(httpClient)
    }

}