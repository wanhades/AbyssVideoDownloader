package com.abmo.providers

import util.getHost
import util.getVParameter


class AbyssToProvider: Provider {

    override fun getVideoID(url: String): String? {
        return when(url.getHost()) {
            "abysscdn.com", "playhydrax.com", "zplayer.io" -> url.getVParameter()
            "short.ink"-> url.substringAfterLast("/")
            else -> url
        }
    }
}