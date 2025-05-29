import com.abmo.di.koinModule
import com.abmo.services.VideoDownloader
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VideoDownloaderIntegrationTest : KoinComponent {

    private val videoDownloader: VideoDownloader by inject()

    @ParameterizedTest
    @MethodSource("videoUrlsAndSlugs")
    fun `test video metadata extraction from real URLs returns correct slug`(url: String, expectedSlug: String) {
        val headers = mapOf("Referer" to "https://abyss.to/")

        val result = videoDownloader.getVideoMetaData(url, headers)

        assertNotNull(result, "Video metadata should not be null for URL: $url")
        assertEquals(expectedSlug, result.slug, "Expected slug '$expectedSlug' for URL: $url")
    }

    companion object {
        @JvmStatic
        fun videoUrlsAndSlugs(): Stream<Arguments> = Stream.of(
            Arguments.of("https://abysscdn.com/?v=K8R6OOjS7", "K8R6OOjS7"),
            Arguments.of("https://abysscdn.com/?v=JZMRhKMkP", "JZMRhKMkP"),
            Arguments.of("https://abysscdn.com/?v=2xvPq9YUT", "2xvPq9YUT"),
            Arguments.of("https://abysscdn.com/?v=CibObsG69", "CibObsG69"),
            Arguments.of("https://abysscdn.com/?v=cAlc2yA_P", "cAlc2yA_P"),
            Arguments.of("https://abysscdn.com/?v=2xvPq9YUT", "2xvPq9YUT"),
            Arguments.of("https://abysscdn.com/?v=Kj1HAeAde", "Kj1HAeAde"),
            Arguments.of("https://abysscdn.com/?v=ZHO0R7ZkR", "ZHO0R7ZkR"),
            Arguments.of("https://abysscdn.com/?v=GZr_NbnAwvD", "GZr_NbnAwvD"),
            Arguments.of("https://abysscdn.com/?v=kzGEXYtPBn", "kzGEXYtPBn")
        )

        @JvmStatic
        @BeforeAll
        fun setUp() {
            startKoin {
                modules(koinModule)
            }
        }
    }
}