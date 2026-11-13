package app.notekea.chat.eval

import app.notekea.ai.data.LlamaLlmEngine
import app.notekea.ai.domain.ModelSetup
import app.notekea.ai.domain.ModelSize
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ActOnRequestUseCase
import app.notekea.chat.domain.AgentAnswerUseCase
import app.notekea.chat.domain.AgentTaskUseCase
import app.notekea.chat.domain.AnswerFromFoundUseCase
import app.notekea.chat.domain.AnswerFromNotesUseCase
import app.notekea.chat.domain.AnswerHelpUseCase
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.CheckPlanUseCase
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.ComposeNotesUseCase
import app.notekea.chat.domain.ConverseUseCase
import app.notekea.chat.domain.DescribeVaultUseCase
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.HelpGuide
import app.notekea.chat.domain.PageProblem
import app.notekea.chat.domain.PlanRequestUseCase
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.embedding.data.LlamaTextEmbedder
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.data.LuceneVectorIndex
import app.notekea.core.notes.data.NioNoteFileAttributes
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioNoteContentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.data.OkioNoteScanner
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.search.domain.FakeIndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.search.domain.PlanIndexUseCase
import app.notekea.shell.data.VaultActionFinder
import app.notekea.shell.data.VaultActionRunner
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.data.OkioNoteMetadataReader
import app.notekea.tree.data.VaultFolderTrash
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.FakeBookmarkStore
import app.notekea.tree.domain.OpenDailyNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Clock

/** The vault actions of a probe, over the real notes on disk and the real model's query translation. */
internal object ProbeRunner {
    fun of(
        store: OkioNoteContentStore,
        scanner: OkioNoteScanner,
        cache: NoteTextCache,
        engine: LlmEngine,
        fileSystem: FileSystem,
        dispatchers: AppDispatchers
    ): VaultActionRunner {
        val operations = OkioNoteFileOperations(fileSystem, dispatchers)
        val settings = ObsidianVaultSettingsReader(fileSystem, dispatchers)
        val apply = ApplyLinkUpdatesUseCase(noteContentStore = store)
        val calendar = JvmLocalCalendar()
        val clock = Clock.System

        val template =
            ApplyTemplateUseCase(FillTemplateUseCase(settings, store, calendar, clock), store)

        val links = PlanLinkUpdatesUseCase(store, JetBrainsMarkdownLinkFinder())

        return VaultActionRunner(
            noteContentStore = store,
            noteFileOperations = operations,
            noteScanner = scanner,
            renameEntry = RenameEntryUseCase(operations, ValidateFileNameUseCase()),
            updateLinksAfterRename = UpdateLinksAfterRenameUseCase(links, apply),
            applyLinkUpdates = apply,
            applyTemplate = template,
            openDailyNote = OpenDailyNoteUseCase(settings, operations, store, calendar, clock),
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
                cache,
                JetBrainsMarkdownLinkFinder(),
                calendar,
                Clock.System
            ),
            appearanceStore = FakeAppearanceStore()
        )
    }
}
