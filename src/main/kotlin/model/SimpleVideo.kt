package model

data class SimpleVideo(
    val slug: String? = null,
    val md5_id: Int? = null,
    val label: String? = null,
    val size: Int? = null,
    var range: Range? = null
)