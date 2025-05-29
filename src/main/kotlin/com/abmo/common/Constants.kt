package com.abmo.common

object Constants {

    /**
     * The default maximum number of concurrent downloads allowed.
     */
    const val DEFAULT_CONCURRENT_DOWNLOAD_LIMIT = 4

    /**
     * Toggle for verbose logging.
     * Set to `true` to enable detailed logs, `false` to disable.
     */
    var VERBOSE = false

    const val ABYSS_BASE_URL = "https://abysscdn.com"

    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"

    val abyssDefaultHeaders = mapOf(
        "Referer" to "$ABYSS_BASE_URL/",
        "Origin" to ABYSS_BASE_URL
    )

}