package app.notekea.notes.presentation.editor

import app.notekea.core.data.JvmLocalTimeFormatter
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.data.ObsidianVaultSettingsReader
import app.notekea.notes.data.OkioAttachmentStore
import app.notekea.notes.data.OkioNoteFileOperations
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.EmbedProblem
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.io.File
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The embeds cases of `MVP_SMOKE_TEST.md` (1.15) on the smoke data itself: `Editor/Embeds/Embeds practice` read in a
 * vault laid out as `smoke.sh prepare` lays out the Playground Vault, so the guide never promises what the data does
 * not show.
 */
@OptIn(ExperimentalPathApi::class)
class SmokeEmbedsTest {
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
    fun `1_15-01 the practice note's embeds show or say why, as the guide lists them`() =
        runTest(context = testDispatcher) {
            assertEquals(
                expected = listOf(
                    "Pantry" to null,
                    "Loop A" to null,
                    "Embeds practice" to EmbedProblem.CIRCULAR,
                    "Shopping basics#Dairy" to null,
                    "Shopping basics#Dessert" to EmbedProblem.MISSING_SECTION,
                    "Blocks#^dough" to null,
                    "Blocks#^corn" to null,
                    "Blocks#^prices" to null,
                    "Blocks#^nothing" to EmbedProblem.MISSING_SECTION,
                    "Weekend menu" to EmbedProblem.MISSING_NOTE
                ),
                actual = practiceViews().filter { it.embed.isOwnLine }.map { it.embed.target to it.problem }
            )
        }

    @Test
    fun `1_15-02 the chain nests three notes deep with the plate, and the loop stops where it comes back`() =
        runTest(context = testDispatcher) {
            val views = practiceViews()
            val pantry = views.first()
            val shelf = pantry.nested.last()
            val bin = shelf.nested.single()

            assertEquals(
                expected = listOf(
                    listOf(AttachmentKind.IMAGE to null, AttachmentKind.NOTE to null),
                    listOf(AttachmentKind.NOTE to null),
                    listOf(AttachmentKind.NOTE to EmbedProblem.TOO_DEEP),
                    listOf(AttachmentKind.NOTE to EmbedProblem.CIRCULAR)
                ),
                actual = listOf(pantry, shelf, bin, views[1].nested.single()).map { view ->
                    view.nested.map { it.kind to it.problem }
                }
            )
        }

    @Test
    fun `1_15-03 the blocks show a paragraph, a list item and a table without their markers`() =
        runTest(context = testDispatcher) {
            assertEquals(
                expected = listOf(
                    "Soak the corn flour for ten minutes,\nthen shape the dough into discs.",
                    "- Buy fresh corn",
                    "| Item | Price |\n|---|---|\n| Flour | 3 |\n| Cheese | 5 |"
                ),
                actual = practiceViews().filter { it.embed.target.startsWith(prefix = "Blocks#^") }
                    .mapNotNull { it.noteText }
            )
        }

    /** The embeds of the practice note, in a vault laid out as `prepare` lays out the Playground Vault. */
    private suspend fun practiceViews(): List<AttachmentView> {
        File(SMOKE_EMBEDS).copyRecursively(target = vault.resolve("Editor/Embeds").toFile())
        File(SHOPPING_NOTE).copyTo(target = vault.resolve("Editor/Shopping basics.md").toFile())
        File(SMOKE_ATTACHMENTS).copyRecursively(target = vault.resolve("attachments").toFile())
        val text = File(SMOKE_EMBEDS, PRACTICE.name).readText()
        return attachments().viewsOf(note = PRACTICE, text = text)
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
        private const val SHOPPING_NOTE = "../smoke-data/notes/Shopping basics.md"
        private const val SMOKE_ATTACHMENTS = "../smoke-data/attachments"
        private const val SMOKE_EMBEDS = "../smoke-data/notes/embeds"
        private const val TEMP_PREFIX = "notekea-smoke-embeds"
        private val PRACTICE = RelativePath(value = "Editor/Embeds/Embeds practice.md")
    }
}
