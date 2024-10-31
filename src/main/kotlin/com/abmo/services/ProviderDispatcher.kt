package com.abmo.services

import com.abmo.providers.*
import com.abmo.executor.JavaScriptExecutor
import com.abmo.util.getHost

class ProviderDispatcher(
    private val javaScriptExecutor: JavaScriptExecutor
) {

    // still this isn't an efficient and clean way to map domains to a provider
    // it will become a mess when more hosts are added
    fun getProviderForUrl(url: String): Provider {
        return when(url.getHost()) {
            "tvphim.my", "tvphim.cx", "tvphim.id" -> TvphimProvider(javaScriptExecutor)
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