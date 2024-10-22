package com.abmo.model

import util.getVParameter
import util.systemDownloadFolder
import java.io.File

data class Config(
    val url: String,
    val resolution: String,
    var outputFile: File? = File(systemDownloadFolder(), "${url.getVParameter()}_$resolution.mp4"),
    val header: Map<String, String>? = null
)