package model

data class Video(
    val ads: Ads? = null,
    val doNotSaveCookies: Boolean? = null,
    val domain: String? = null,
    val fullscreenOrientationLock: String? = null,
    val height: String? = null,
    val id: String? = null,
    val md5_id: Int? = null,
    val pipIcon: String? = null,
    val preload: String? = null,
    val slug: String? = null,
    val sources: List<Source?>? = null,
    val user_id: Int? = null,
    val width: String? = null
)

fun Video.toSimpleVideo(resolution: String): SimpleVideo {
    val source = sources?.find { it?.label == resolution }
    return SimpleVideo(
        slug = slug,
        md5_id = md5_id,
        label = source?.label,
        size = source?.size
    )
}
