package com.abmo.model

import com.abmo.Constant.DEFAULT_CONCURRENT_DOWNLOAD_LIMIT
import java.io.File

data class Config(
    val url: String,
    val resolution: String,
    var outputFile: File?,
    val header: Map<String, String>? = null,
    val connections: Int = DEFAULT_CONCURRENT_DOWNLOAD_LIMIT
)