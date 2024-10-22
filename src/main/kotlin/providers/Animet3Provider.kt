package com.abmo.providers

import com.mashape.unirest.http.Unirest
import util.fetchDocument
import util.getVParameter
import util.toJsoupDocument


class Animet3Provider: Provider {

    override fun getVideoID(url: String): String? {
        val regex = Regex("""MovieId\s*=\s*'(\d+)',\s*EpisodeId\s*=\s*'(\d+)',""")
        val matchResult = regex.find(url.fetchDocument().html()) ?: return null

        val movieId = matchResult.groupValues[1]
        val episodeId = matchResult.groupValues[2]

        val response = Unirest.post("https://animet3.biz/ajax/player_hrx")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .body("id=$movieId&ep=$episodeId&sv=hr-0")
            .asString().body

        val videoID = response.toJsoupDocument()
            .select("iframe")
            .attr("src").getVParameter()

        return videoID
    }

}