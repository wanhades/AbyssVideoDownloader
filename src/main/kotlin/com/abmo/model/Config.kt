package com.abmo.model

import com.abmo.util.getVParameter
import com.abmo.util.systemDownloadFolder
import java.io.File

data class Config(
    val url: String,
    val resolution: String,
    var outputFile: File? = File(systemDownloadFolder(), "${url.getVParameter()}_$resolution.mp4"),
    val header: Map<String, String>? = null
)