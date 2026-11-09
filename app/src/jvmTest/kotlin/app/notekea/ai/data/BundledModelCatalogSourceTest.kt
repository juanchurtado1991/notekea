package app.notekea.ai.data

import app.notekea.ai.domain.ModelSize
import app.notekea.core.data.JvmAppDispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The bundled catalog has the three sizes, pinned to a commit and with their SHA-256. */
class BundledModelCatalogSourceTest {
    @Test
    fun `the catalog reads the three sizes and the reranker, each pinned and checked`() =
        runBlocking {
            val catalog = BundledModelCatalogSource(dispatchers = JvmAppDispatchers()).load()
            val files = catalog.models.map { it.file } + catalog.reranker

            assertEquals(
                expected = ModelSize.entries,
                actual = catalog.models.map { it.size }
            )

            assertTrue(
                actual = files.all {
                    PINNED_URL.matches(input = it.url) &&
                        it.url.endsWith(suffix = "/${it.fileName}")
                }
            )

            assertTrue(
                actual = files.all {
                    SHA_256.matches(input = it.sha256) && it.sizeBytes > 0
                }
            )

            assertEquals(
                expected = listOf(8, 16, 32),
                actual = catalog.models.map { it.minimumMemoryGb }
            )
        }

    private companion object {
        val PINNED_URL = Regex(
            pattern = "https://huggingface\\.co/[\\w.-]+/[\\w.-]+/resolve/[0-9a-f]{40}/[\\w.-]+\\.gguf"
        )
        val SHA_256 = Regex(pattern = "[0-9a-f]{64}")
    }
}
