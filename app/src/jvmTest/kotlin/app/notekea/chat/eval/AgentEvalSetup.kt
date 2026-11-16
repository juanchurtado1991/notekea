package app.notekea.chat.eval

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.search.eval.EvalVault
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.data.OkioNoteMetadataReader
import okio.FileSystem
import java.io.File
import java.nio.file.Files
import kotlin.time.Clock

/** What the agent's evaluation works on: the vault's notes with their metadata, the context, and the runner. */
internal object AgentEvalSetup {
    /** The vault's notes with the properties, tags and tasks of their committed text, read from a copy on disk. */
    suspend fun withMetadata(
        indexed: EvalVault,
        dispatchers: AppDispatchers
    ): List<NoteEntry> {
        val copy = Files.createTempDirectory("notekea-agent").toFile()

        val reader =
            OkioNoteMetadataReader(FileSystem.SYSTEM, dispatchers, GhostFrontmatterParser())

        return indexed.notes.map { note ->
            val target = File(copy, note.file.path.value)
            target.parentFile.mkdirs()

            target.writeText(
                text = indexed
                    .store
                    .texts
                    .getValue(note.file.path)
            )

            note.copy(metadata = reader.read(copy.path, note.file.path) ?: note.metadata)
        }
    }

    /** [notes] with the chat's memory written into the vault (`docs/agent-memory.md`), when there is one. */
    fun remembering(
        indexed: EvalVault,
        notes: List<NoteEntry>,
        memory: String?
    ): List<NoteEntry> {
        if (memory == null) return notes
        indexed.store.texts[VaultGuide.MEMORY] = memory

        return notes + NoteFixtures.entry(path = VaultGuide.MEMORY.value)
    }

    fun contextOf(notes: List<NoteEntry>) = ActionContext(
        today = "2026-11-11",
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

    // Nothing is ever run in the evaluation: the runner's find is the vault's, the rest a fake.
    fun runnerOf(
        indexed: EvalVault,
        cache: NoteTextCache,
        engine: LlmEngine
    ): ActionRunner {
        val apply = ApplyLinkUpdatesUseCase(noteContentStore = indexed.store)
        val calendar = JvmLocalCalendar()

        val finder = VaultActionFinder(
            TranslateQueryUseCase(engine, GhostQueryJsonReader()),
            BuildQueryCatalogUseCase(),
            RunQueryUseCase(),
            QueryYaml(),
            ListTasksUseCase(),
            SetTaskDoneUseCase(indexed.store, apply),
            SetPropertyUseCase(indexed.store, apply),
            cache,
            JetBrainsMarkdownLinkFinder(),
            calendar,
            Clock.System
        )

        return object : ActionRunner by FakeActionRunner() {
            override suspend fun find(
                vaultPath: String,
                step: ActionStep,
                notes: Collection<NoteEntry>
            ) = finder.find(
                vaultPath = vaultPath,
                step = step,
                notes = notes
            )
        }
    }
}
