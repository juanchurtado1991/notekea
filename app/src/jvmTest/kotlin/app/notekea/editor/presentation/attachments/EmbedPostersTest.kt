package app.notekea.editor.presentation.attachments

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioAttachmentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.data.JvmLocalTimeFormatter
import app.notekea.editor.domain.AttachFileUseCase
import app.notekea.editor.domain.FakeYouTubeDetails
import app.notekea.editor.domain.FilePosters
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/** The pictures of the PDFs and videos a note embeds (the developer's smoke of 2026-11-13: none had one). */
@OptIn(ExperimentalPathApi::class)
class EmbedPostersTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault = Files.createTempDirectory(TEMP_PREFIX)
    private val asked = mutableListOf<String>()

    private val store = OkioAttachmentStore(
        fileSystem = FileSystem.SYSTEM,
        dispatchers = dispatchers
    )

    private val settings = ObsidianVaultSettingsReader(
        fileSystem = FileSystem.SYSTEM,
        dispatchers = dispatchers
    )

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `a PDF and a video get their picture once made, each asked for once, and a zip gets none`() =
        runTest(context = testDispatcher) {
            listOf("menu.pdf", "trip.mp4", "bag.zip").forEach {
                Files.write(vault.resolve(it), byteArrayOf(1))
            }

            val attachments = attachments()
            val note = RelativePath(value = "Menu.md")
            val text = "![[menu.pdf]]\n![[trip.mp4]]\n![[bag.zip]]\n"

            val before = attachments.viewsOf(
                note = note,
                text = text
            )

            val made = attachments.makePosters(views = before)

            val after = attachments.viewsOf(
                note = note,
                text = text
            )

            val again = attachments.makePosters(views = after)

            assertEquals(
                expected = listOf<Any>(
                    listOf(false, false, false),
                    true,
                    listOf(true, true, false),
                    false,
                    listOf("menu.pdf", "trip.mp4")
                ),
                actual = listOf(
                    before.map {
                        it.image != null
                    },
                    made,
                    after.map {
                        it.image != null
                    },
                    again,
                    asked
                )
            )
        }

    private fun pictured(views: List<AttachmentView>): List<Boolean> = views.map {
        it.image != null
    }

    private fun attachments() = EditorAttachments(
        vaultPath = vault.toString(),
        attachmentStore = store,
        vaultSettingsReader = settings,
        attachFile = AttachFileUseCase(
            attachmentStore = store,
            vaultSettingsReader = settings,
            noteFileOperations = OkioNoteFileOperations(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = dispatchers
            ),
            localTimeFormatter = JvmLocalTimeFormatter(),
            clock = Clock.System
        ),
        youTubeDetails = FakeYouTubeDetails(),
        dispatchers = dispatchers,
        filePosters = FilePosters { _, path ->
            asked += path.value
            PICTURE
        }
    )

    companion object {
        private const val TEMP_PREFIX = "notekea-posters"

        private val PICTURE = ByteArrayOutputStream().use { bytes ->
            ImageIO.write(BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes)
            bytes.toByteArray()
        }
    }
}
