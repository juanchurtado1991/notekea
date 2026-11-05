package app.notekea.query.presentation

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryPreferencesStore
import app.notekea.query.domain.QueryResult
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SaveQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.VaultQuery
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The Query tab (`ROADMAP.md` 2.10) for the vault at [vaultPath], and the writes of the query blocks in its notes. The
 * query survives between runs, every change runs it again off the UI thread, and nothing runs while its tab is closed.
 * A cell changes its note, whose row shows the new value at once, before the vault's scan brings it; blocks run
 * against the same notes, and their state's [QueryState.notesVersion] tells them to run again.
 */
class QueryViewModel(
    private val vaultPath: String,
    private val runQuery: RunQueryUseCase,
    private val buildCatalog: BuildQueryCatalogUseCase,
    private val setProperty: SetPropertyUseCase,
    private val saveQuery: SaveQueryUseCase,
    private val preferencesStore: QueryPreferencesStore,
    private val dispatchers: AppDispatchers
) : BaseViewModel<QueryState, QueryIntent, QueryEffect>(initialState = QueryState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var notes: Collection<NoteEntry> = emptyList()
    private var isShown = false
    private var isQueryRead = false
    private var refreshJob: Job? = null
    private val queryWrites = Mutex()

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            val query = preferencesStore.read(vaultPath = vaultPath)
            isQueryRead = true
            reduce { it.copy(query = query) }
            refresh()
        }
    }

    override fun handleIntent(
        intent: QueryIntent
    ) {
        when (intent) {
            is QueryIntent.OnNotesChange -> setNotes(changed = intent.notes)

            is QueryIntent.OnShownChange -> {
                isShown = intent.isShown
                refresh()
            }

            is QueryIntent.OnNoteOpen -> emit(effect = QueryEffect.OpenNote(path = intent.path))

            is QueryIntent.OnCellChange -> write(path = intent.path, property = intent.property)

            is QueryIntent.OnSave -> save(path = intent.path)

            QueryIntent.OnNoticeDismiss -> reduce { it.copy(notice = null) }

            else -> onQueryChange(intent = intent)
        }
    }

    /** The intents of the query's own controls. */
    private fun onQueryChange(
        intent: QueryIntent
    ) {
        when (intent) {
            is QueryIntent.OnFilterAdd -> change { it.copy(filters = it.filters + intent.filter) }

            is QueryIntent.OnFilterChange -> change { query ->
                query.copy(
                    filters = query.filters.mapIndexed { i, old ->
                        if (i ==
                            intent.index
                        ) {
                            intent.filter
                        } else {
                            old
                        }
                    }
                )
            }

            is QueryIntent.OnFilterRemove -> change { query ->
                query.copy(filters = query.filters.filterIndexed { i, _ -> i != intent.index })
            }

            is QueryIntent.OnSortChange -> change { it.copy(sort = intent.sort) }

            is QueryIntent.OnColumnToggle -> change { toggled(query = it, column = intent.column) }

            else -> Unit
        }
    }

    /** The columns with [column] shown or hidden; choosing the first keeps the ones shown until now. */
    private fun toggled(
        query: VaultQuery,
        column: String
    ): VaultQuery {
        val shown = query.shownColumns
        return query.copy(columns = if (column in shown) shown - column else shown + column)
    }

    /** What [query], a block's, finds in the vault as this view model knows it. */
    suspend fun resultOf(
        query: VaultQuery
    ): QueryResult {
        val shown = notes
        return withContext(context = dispatchers.default) { runQuery(query, shown) }
    }

    private fun setNotes(
        changed: Collection<NoteEntry>
    ) {
        notes = changed
        reduce { it.copy(notesVersion = it.notesVersion + 1) }
        refresh()
    }

    private fun change(
        update: (VaultQuery) -> VaultQuery
    ) {
        reduce { it.copy(query = update(it.query)) }
        launchGuarded(onError = ::logUnexpectedError) {
            // The latest query, in turn: of two quick changes, the first must not be the one that stays.
            queryWrites.withLock { preferencesStore.write(vaultPath = vaultPath, query = state.value.query) }
        }
        refresh()
    }

    private fun refresh() {
        if (!isShown || !isQueryRead) return
        val query = state.value.query
        val shown = notes
        refreshJob?.cancel()
        refreshJob = launchGuarded(onError = ::logUnexpectedError) {
            val (result, catalog) = withContext(context = dispatchers.default) {
                runQuery(query, shown) to buildCatalog(shown)
            }
            reduce { it.copy(result = result, catalog = catalog, isLoading = false) }
        }
    }

    private fun write(
        path: RelativePath,
        property: NoteProperty
    ) {
        launchGuarded(onError = ::showFailure) {
            val rewrite = setProperty(vaultPath = vaultPath, path = path, property = property)
            if (finishWrite(rewrite = rewrite)) {
                setNotes(changed = notes.map { if (it.file.path == path) withProperty(it, property) else it })
            }
        }
    }

    private fun save(
        path: RelativePath
    ) {
        val query = state.value.query
        launchGuarded(onError = ::showFailure) {
            val rewrite = saveQuery(vaultPath = vaultPath, path = path, query = query)
            if (finishWrite(rewrite = rewrite)) reduce { it.copy(notice = QueryNotice.Saved(path = path)) }
        }
    }

    /** Tells the editors of the note written, or the person that none was; whether it was. */
    private fun finishWrite(
        rewrite: NoteRewrite?
    ): Boolean {
        if (rewrite == null) {
            reduce { it.copy(notice = QueryNotice.WriteFailed) }
            return false
        }
        emit(effect = QueryEffect.NoteRewritten(path = rewrite.path))
        return true
    }

    private fun withProperty(
        entry: NoteEntry,
        property: NoteProperty
    ): NoteEntry {
        val properties = entry.metadata.properties
        val isKnown = properties.any { it.key == property.key }
        val changed = if (isKnown) {
            properties.map { if (it.key == property.key) property else it }
        } else {
            properties + property
        }
        return entry.copy(metadata = entry.metadata.copy(properties = changed))
    }

    private fun showFailure(
        throwable: Throwable
    ) {
        logUnexpectedError(throwable = throwable)
        reduce { it.copy(notice = QueryNotice.WriteFailed) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR }
    }

    private companion object {
        const val LOG_TAG = "Query"
        const val UNEXPECTED_ERROR = "Unexpected query failure"
    }
}
