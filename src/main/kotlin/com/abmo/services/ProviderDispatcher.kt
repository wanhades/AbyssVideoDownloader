package com.abmo.services

import com.abmo.providers.*
import com.abmo.util.JavaScriptExecutor
import com.abmo.util.getHost

class ProviderDispatcher(
    private val javaScriptExecutor: JavaScriptExecutor
) {

    fun getProviderForUrl(url: String): Provider {
        return when(url.getHost()) {
            "tvphim.my", "tvphim.cx" -> TvphimProvider(javaScriptExecutor)
            "sieutamphim.com" -> SieutamphimProvider()
            "phimbet.biz" -> PhimbetProvider()
            "fimmoi.top" -> FimmoiProvider()
            "motchill.taxi", "motchill.to", "subnhanh.win" -> MotchillProvider()
            "animet3.biz" -> Animet3Provider()
            "tvhayw.org" -> TvhaywProvider()

            else -> AbyssToProvider()
        }
    }

}