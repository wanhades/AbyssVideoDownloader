package com.abmo.model

import java.io.File

data class Config(
    val url: String,
    val resolution: String,
    var outputFile: File?,
    val header: Map<String, String>? = null,
    val connections: Int = 6
)