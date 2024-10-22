package com.abmo.providers

import com.mashape.unirest.http.Unirest
import util.fetchDocument
import util.getVParameter
import util.toJsoupDocument


class MotchillProvider: Provider {

    override fun getVideoID(url: String): String? {
        val queryParam = url.fetchDocument()
            .select("div.block-wrapper.text-center a")
            .find { it.toString().contains("type=hii") }
            ?.attr("data-href")

        val response = Unirest.post("https://motchill.taxi$queryParam")
            .header("x-requested-with", "XMLHttpRequest")
            .header("Referer", url)
            .asString().body

        val videoID = response.toJsoupDocument()
            .getElementById("playerIframe")
            ?.attr("src")?.getVParameter()


        return videoID
    }

}