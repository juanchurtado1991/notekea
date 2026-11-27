package app.notekea.chat.eval

import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.data.JvmLocalCalendar
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioNoteContentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.data.OkioNoteScanner
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.UpdateLinksAfterRenameUseCase
import app.notekea.core.notes.domain.templates.FillTemplateUseCase
import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.shell.data.chat.VaultActionRunner
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tree.data.VaultFolderTrash
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.FakeBookmarkStore
import app.notekea.tree.domain.OpenDailyNoteUseCase
import okio.FileSystem
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
