package app.notekea.notes.presentation.editor

import app.notekea.core.data.JvmLocalTimeFormatter
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.data.ObsidianVaultSettingsReader
import app.notekea.notes.data.OkioAttachmentStore
import app.notekea.notes.data.OkioNoteFileOperations
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.EmbedProblem
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * Embedded notes on real files (`ROADMAP.md` 1.15): a temporary vault whose notes embed each other in a chain, around
 * in a circle, by a block and towards a note that is not there, read by the real adapters.
 */
@OptIn(ExperimentalPathApi::class)
class EmbedsOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault = Files.createTempDirectory(TEMP_PREFIX)
    private val store = OkioAttachmentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)
    private val settings = ObsidianVaultSettingsReader(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `a chain of embedded notes, a circle, a block and a missing note are shown as they are on disk`() =
        runTest(context = testDispatcher) {
            mapOf(
                "Recipes/Arepas.md" to "Dough first ^dough\n\n![[Pantry]]",
                "Pantry.md" to "---\ntags: [home]\n---\nCorn flour\n![[Kitchen/Shelf]]",
                "Kitchen/Shelf.md" to "Top shelf\n![[Pantry]]"
            ).forEach { (name, text) ->
                vault.resolve(name).also { Files.createDirectories(it.parent) }.let { Files.writeString(it, text) }
            }

            val views = attachments().viewsOf(
                note = RelativePath(value = "Menu.md"),
                text = "![[Arepas#^dough]]\n![[Arepas]]\n![[Weekend menu]]\n"
            )

            val pantry = views[1].nested.single()
            val shelf = pantry.nested.single()
            assertEquals(
                expected = listOf(
                    Triple("Arepas#^dough", "Dough first", null),
                    Triple("Arepas", "Dough first ^dough\n\n![[Pantry]]", null),
                    Triple("Weekend menu", null, EmbedProblem.MISSING_NOTE),
                    Triple("Pantry", "Corn flour\n![[Kitchen/Shelf]]", null),
                    Triple("Kitchen/Shelf", "Top shelf\n![[Pantry]]", null),
                    Triple("Pantry", null, EmbedProblem.CIRCULAR)
                ),
                actual = (views + pantry + shelf + shelf.nested).map {
                    Triple(it.embed.target, it.noteText, it.problem)
                }
            )
        }

    private fun attachments() = EditorAttachments(
        vaultPath = vault.toString(),
        attachmentStore = store,
        vaultSettingsReader = settings,
        attachFile = AttachFileUseCase(
            attachmentStore = store,
            vaultSettingsReader = settings,
            noteFileOperations = OkioNoteFileOperations(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers),
            localTimeFormatter = JvmLocalTimeFormatter(),
            clock = Clock.System
        ),
        dispatchers = dispatchers
    )

    companion object {
        private const val TEMP_PREFIX = "notekea-embeds"
    }
}
