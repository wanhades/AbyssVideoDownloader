package com.abmo.model

import util.getVideoID
import util.systemDownloadFolder
import java.io.File

data class Config(
    val url: String,
    val resolution: String,
    var outputFile: File? = File(systemDownloadFolder(), "${url.getVideoID()}_$resolution.mp4")
)