package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ActOnRequestUseCase
import app.notekea.chat.domain.ActionContext
import app.notekea.chat.domain.ActionTurnStep
import app.notekea.chat.domain.CheckPlanUseCase
import app.notekea.chat.domain.ComposeNotesUseCase
import app.notekea.chat.domain.PlanRequestUseCase
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.data.NioNoteFileAttributes
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioNoteContentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.data.OkioNoteScanner
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.shell.data.VaultActionFinder
import app.notekea.shell.data.VaultActionRunner
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.data.OkioNoteMetadataReader
import app.notekea.tree.data.VaultFolderTrash
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.FakeBookmarkStore
import app.notekea.tree.domain.OpenDailyNoteUseCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The chat's actions from request to the disk (3.9) with the real model, on a copy of the committed Sample Vault: each
 * request is planned, written, done, checked in the files and undone, which must leave every file as it was; what
 * each wrote goes to `build/reports/action-run.md`. Run by hand like [AnswerEvalChatModelTest].
 */
class ActionRunChatModelTest {
    private val folder = Path.of(System.getProperty("notekea.chat.models").orEmpty())
    private val size =
        ModelSize.fromId(id = System.getProperty("notekea.chat.size")) ?: ModelSize.MEDIUM
    private val dispatchers = JvmAppDispatchers()
    private val vault = Files
        .createTempDirectory("notekea-actions")
        .resolve("Sample Vault")
        .toFile()
    private val fileSystem = FileSystem.SYSTEM
    private val calendar = JvmLocalCalendar()
    private val store = OkioNoteContentStore(fileSystem, dispatchers, NioNoteFileAttributes())
    private val operations = OkioNoteFileOperations(fileSystem, dispatchers)
    private val scanner = OkioNoteScanner(fileSystem, dispatchers)
    private val settings = ObsidianVaultSettingsReader(fileSystem, dispatchers)
    private val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)

    private val setup = ModelSetup(
        size = size,
        modelPath = folder.resolve(FILES.getValue(size)).toString(),
        rerankerPath = folder.resolve("bge-reranker-v2-m3-Q4_K_M.gguf").toString(),
        slots = GPU_SLOTS,
        contextTokens = ModelSetup.CONTEXT_TOKENS,
        onGpu = true
    )

    private val engine = LlamaLlmEngine(
        setupSource = { Outcome.Success(value = setup) },
        nativeFolder = Files.createTempDirectory("notekea-natives").toString(),
        dispatchers = dispatchers
    )

    private val runner = VaultActionRunner(
        noteContentStore = store,
        noteFileOperations = operations,
        noteScanner = scanner,
        renameEntry = RenameEntryUseCase(operations, ValidateFileNameUseCase()),
        updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
            PlanLinkUpdatesUseCase(store, JetBrainsMarkdownLinkFinder()),
            apply
        ),
        applyLinkUpdates = apply,
        applyTemplate = ApplyTemplateUseCase(
            FillTemplateUseCase(settings, store, calendar, Clock.System),
            store
        ),
        openDailyNote = OpenDailyNoteUseCase(settings, operations, store, calendar, Clock.System),
        bookmarkStore = FakeBookmarkStore(),
        trash = VaultFolderTrash(fileSystem, dispatchers),
        finder = VaultActionFinder(
            TranslateQueryUseCase(engine, GhostQueryJsonReader()),
            BuildQueryCatalogUseCase(),
            RunQueryUseCase(),
            QueryYaml(),
            ListTasksUseCase(),
            SetTaskDoneUseCase(store, apply),
            SetPropertyUseCase(store, apply),
            NoteTextCache(store, TextFolder()),
            JetBrainsMarkdownLinkFinder(),
            calendar,
            Clock.System
        ),
        appearanceStore = FakeAppearanceStore()
    )

    private val act = ActOnRequestUseCase(
        planRequest = PlanRequestUseCase(engine, GhostActionPlanReader()),
        checkPlan = CheckPlanUseCase(),
        composeNotes = ComposeNotesUseCase(
            engine,
            NoteTextCache(store, TextFolder()),
            GhostActionPlanReader()
        ),
        actionRunner = runner
    )

    @AfterTest
    fun tearDown() {
        engine.close()
    }

    @Test
    fun `every request is written as asked, and Undo leaves the vault as it was`() = runBlocking {
        assumeTrue(Files.isRegularFile(Path.of(setup.modelPath)))
        copySampleVault()

        val report = StringBuilder("# Actions on the Sample Vault (${size.id})\n")
        val failures = mutableListOf<String>()

        ActionRunCases.all.forEach { case ->
            val before = snapshot()

            val outcome = runCase(
                case = case,
                report = report
            )

            if (outcome != null) failures += "${case.request.lines().first()}: $outcome"
            if (snapshot() != before) failures += "${case.request.lines().first()}: Undo left the vault changed"
        }

        File("build/reports/action-run.md").writeText(text = report.toString())

        assertEquals(
            expected = emptyList(),
            actual = failures,
            message = failures.joinToString(separator = "\n")
        )
    }

    /** Plans, writes, does and undoes [case]; the failure found, or null. */
    private suspend fun runCase(
        case: ActionRunCase,
        report: StringBuilder
    ): String? {
        val notes = notes()

        val open = case.open?.let { name ->
            notes.map { it.file.path }.first {
                it.nameWithoutExtension == name
            }
        }

        val steps = act(
            request = case.request,
            vaultPath = vault.path,
            notes = notes,
            context = contextOf(
                notes = notes,
                open = open
            ),
            openNote = open
        ).toList()

        val proposed = steps.filterIsInstance<ActionTurnStep.Proposed>().singleOrNull()
            ?: return "no card: ${steps.last()}"

        report.append("\n## ${case.request.lines().first()}\n\n")

        proposed.steps.forEach { step ->
            report.append("- ${step.kind.written} ${step.notes.map { it.value }} ${step.title.orEmpty()}\n")
            step.content.forEach { report.append("\n### ${it.path.value}\n\n${it.text}\n") }
        }

        val done = runner.run(
            vaultPath = vault.path,
            steps = proposed.steps,
            notes = notes
        ) as? Outcome.Success
            ?: return "not done"

        val problem = case.check(vault)

        runner.undo(
            vaultPath = vault.path,
            done = done.value
        )

        return problem
    }

    private suspend fun notes(): List<NoteEntry> {
        val listing = (scanner.scan(vaultPath = vault.path) as Outcome.Success).value
        val reader = OkioNoteMetadataReader(fileSystem, dispatchers, GhostFrontmatterParser())

        return listing.files.map {
            NoteEntry(it, reader.read(vault.path, it.path) ?: NoteMetadata.NONE)
        }
    }

    private fun contextOf(
        notes: List<NoteEntry>,
        open: RelativePath?
    ) = ActionContext(
        today = "2026-11-10",
        openNote = open?.value?.removeSuffix(suffix = ".md"),
        folders = ActionContext.foldersOf(paths = notes.map { it.file.path }),
        notes = notes.map {
            it
                .file
                .path
                .value
                .removeSuffix(suffix = ".md")
        },
        properties = notes.flatMap { note ->
            note
                .metadata
                .properties
                .map { it.key }
        }.distinct(),
        tags = notes.flatMap { it.metadata.tags }.distinct()
    )

    /** Every file of the vault but its trash, with its text. */
    private fun snapshot(): Map<String, String> = vault
        .walkTopDown()
        .filter {
            it.isFile && !it.relativeTo(vault).path.startsWith(".trash")
        }
        .associate {
            it.relativeTo(vault).invariantSeparatorsPath to it.readText()
        }

    private fun copySampleVault() {
        val root = "smoke-data/vaults/Sample Vault/"

        git("ls-files", "-z", "--", root)
            .split('\u0000')
            .filter { it.isNotEmpty() }
            .forEach { file ->
                val target = File(vault, file.removePrefix(prefix = root))
                target.parentFile.mkdirs()
                target.writeBytes(gitBytes("show", "HEAD:$file"))
            }
    }

    private fun git(vararg arguments: String): String = gitBytes(*arguments).decodeToString()

    private fun gitBytes(vararg arguments: String): ByteArray {
        val process =
            ProcessBuilder(listOf("git") + arguments).directory(File("..").canonicalFile).start()

        val output = process.inputStream.readBytes()
        check(process.waitFor() == 0) { "git ${arguments.first()} failed" }

        return output
    }

    private companion object {
        const val GPU_SLOTS = 4
        val FILES = mapOf(
            ModelSize.SMALL to "Qwen3.5-2B-Q4_K_M.gguf",
            ModelSize.MEDIUM to "Qwen3.5-4B-Q4_K_M.gguf",
            ModelSize.LARGE to "Qwen3.5-9B-Q4_K_M.gguf"
        )
    }
}
