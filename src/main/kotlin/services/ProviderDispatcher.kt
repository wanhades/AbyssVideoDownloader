package com.abmo.services

import com.abmo.providers.AbyssToProvider
import com.abmo.providers.Provider
import com.abmo.providers.TvphimProvider
import com.abmo.util.JavaScriptRunner

class ProviderDispatcher(
    private val javaScriptRunner: JavaScriptRunner
) {

    fun getProviderForUrl(url: String): Provider {
        return when {
            url.contains("tvphim.my") -> TvphimProvider(javaScriptRunner)

            else -> AbyssToProvider()
        }
    }

}