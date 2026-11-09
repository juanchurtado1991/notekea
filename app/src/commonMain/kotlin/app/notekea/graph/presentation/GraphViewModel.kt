package app.notekea.graph.presentation

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FindRelatedNotesUseCase
import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import app.notekea.graph.domain.BuildNoteGraphUseCase
import app.notekea.graph.domain.GraphLayout
import app.notekea.graph.domain.GraphPreferences
import app.notekea.graph.domain.GraphPreferencesStore
import app.notekea.graph.domain.LinkNotesUseCase
import app.notekea.graph.domain.NoteGraph
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * The graph tab for the vault at [vaultPath]. It opens on the note it was opened from and its neighbors, on rings; with
 * no note it asks for one. Choosing a note around another moves it to the center. Every change replaces the work under
 * way, the layout runs off the UI thread, and nothing runs while its tab is closed.
 */
class GraphViewModel(
    private val vaultPath: String,
    private val buildNoteGraph: BuildNoteGraphUseCase,
    findRelatedNotes: FindRelatedNotesUseCase,
    private val linkNotes: LinkNotesUseCase,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val fuzzyMatcher: FuzzyMatcher,
    noteTextCache: NoteTextCache,
    markdownLineStripper: MarkdownLineStripper,
    private val preferencesStore: GraphPreferencesStore,
    private val dispatchers: AppDispatchers
) : BaseViewModel<GraphState, GraphIntent, GraphEffect>(initialState = GraphState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val layout = GraphLayout()
    private var notes: Map<RelativePath, NoteEntry> = emptyMap()
    private var graph: NoteGraph? = null
    private var openedFrom: RelativePath? = null
    private val relatedNotes = GraphRelatedNotes(vaultPath, findRelatedNotes, noteTextCache)
    private val previews = GraphPreviews(vaultPath, noteTextCache, markdownLineStripper)
    private var isShown = false
    private var refreshJob: Job? = null
    private var previewJob: Job? = null
    private var linkJob: Job? = null
    private var lastLink: NoteRewrite? = null

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            val preferences = preferencesStore.read(vaultPath = vaultPath)

            reduce { it.copy(excludedFolders = preferences.excludedFolders) }

            refresh()
        }
    }

    override fun handleIntent(intent: GraphIntent) {
        when (intent) {
            is GraphIntent.OnNotesChange -> changeNotes(notes = intent.notes)

            is GraphIntent.OnIndexChange -> changeIndex(isReady = intent.isReady)

            is GraphIntent.OnShownChange -> changeShown(isShown = intent.isShown)

            is GraphIntent.OnOpen -> open(note = intent.note)

            is GraphIntent.OnScopeSelect -> selectScope(scope = intent.scope)

            is GraphIntent.OnDepthSelect -> change { it.copy(depth = intent.depth) }

            is GraphIntent.OnNoteSelect -> select(path = intent.path)

            is GraphIntent.OnMove -> move(direction = intent.direction)

            GraphIntent.OnOpenSelected ->
                state
                    .value
                    .selected
                    ?.let { emit(effect = GraphEffect.OpenNote(path = it)) }

            is GraphIntent.OnNoteOpen -> emit(effect = GraphEffect.OpenNote(path = intent.path))

            GraphIntent.OnBack -> openedFrom?.let { emit(effect = GraphEffect.OpenNote(path = it)) }

            GraphIntent.OnInspectorToggle -> emit(effect = GraphEffect.ToggleInspector)

            GraphIntent.OnRelatedToggle -> change { it.copy(showsRelated = !it.showsRelated) }

            GraphIntent.OnOrphansToggle -> change { it.copy(showsOrphans = !it.showsOrphans) }

            is GraphIntent.OnFolderToggle -> toggleFolder(folder = intent.folder)

            GraphIntent.OnSearchOpen -> reduce { it.copy(isSearching = true) }

            GraphIntent.OnSearchClose -> closeSearch()

            is GraphIntent.OnQueryChange -> changeQuery(query = intent.query)

            is GraphIntent.OnMatchStep -> stepMatch(isForward = intent.isForward)

            is GraphIntent.OnLink -> link(target = intent.path)

            GraphIntent.OnLinkUndo -> undoLink()
        }
    }

    private fun changeNotes(notes: Collection<NoteEntry>) {
        this.notes = notes.associateBy { it.file.path }
        graph = null
        relatedNotes.forget()
        refresh()
    }

    private fun changeIndex(isReady: Boolean) {
        if (isReady == state.value.isIndexReady) return
        relatedNotes.forget()

        change { it.copy(isIndexReady = isReady) }
    }

    private fun changeShown(isShown: Boolean) {
        this.isShown = isShown
        refresh()
    }

    private fun open(note: RelativePath?) {
        openedFrom = note

        change {
            it.copy(
                center = note,
                selected = note,
                scope = GraphScope.THIS_NOTE
            )
        }
    }

    private fun selectScope(scope: GraphScope) {
        change {
            it.copy(
                scope = scope,
                center = it.selected ?: it.center
            )
        }
    }

    private fun select(path: RelativePath) {
        reduce {
            it.copy(
                linked = null,
                linkFailed = false
            )
        }

        if (state.value.scope == GraphScope.THIS_NOTE) {
            change {
                it.copy(
                    center = path,
                    selected = path
                )
            }
        } else {
            reduce { it.copy(selected = path) }

            showPreviewNow()
        }
    }

    private fun move(direction: GraphDirection) {
        val current = state.value
        val selected = current.selected ?: return

        noteTowards(direction, selected, current.dots, current.lines)?.let { select(path = it) }
    }

    private fun closeSearch() {
        reduce {
            it.copy(
                isSearching = false,
                query = "",
                matches = emptyList(),
                matchIndex = -1
            )
        }
    }

    private fun changeQuery(query: String) {
        reduce {
            withMatches(
                state = it.copy(
                    query = query,
                    matchIndex = -1
                )
            )
        }
    }

    private fun withMatches(state: GraphState): GraphState {
        val paths = graph
            ?.withoutFolders(folders = state.excludedFolders)
            ?.nodes
            ?.map { it.path }
            .orEmpty()

        val matches = matchesOf(
            query = state.query,
            paths = paths,
            matcher = fuzzyMatcher
        )

        val matchIndex = state.matchIndex.coerceAtMost(maximumValue = matches.lastIndex)

        return state.copy(
            matches = matches,
            matchIndex = matchIndex
        )
    }

    private fun stepMatch(isForward: Boolean) {
        val current = state.value
        val count = current.matches.size
        if (count == 0) return

        val index = when {
            current.matchIndex >= 0 -> (current.matchIndex + if (isForward) 1 else count - 1) %
                count

            isForward -> 0

            else -> count - 1
        }

        val path = current.matches[index]

        change {
            it.copy(
                scope = GraphScope.THIS_NOTE,
                center = path,
                selected = path,
                matchIndex = index,
                reveals = it.reveals + 1,
                linked = null,
                linkFailed = false
            )
        }
    }

    private fun link(target: RelativePath) {
        val source = state.value.preview?.path ?: return
        if (linkJob?.isActive == true) return

        linkJob = launchGuarded(onError = ::logUnexpectedError) {
            lastLink = linkNotes(vaultPath, source, target, notes.keys)
            val isLinked = lastLink != null

            if (isLinked) {
                graph = graph?.withLink(
                    one = source,
                    other = target
                )

                emit(effect = GraphEffect.NoteRewritten(path = source))
            }

            change {
                it.copy(
                    linked = target.takeIf { isLinked },
                    linkFailed = !isLinked
                )
            }
        }
    }

    private fun undoLink() {
        val rewrite = lastLink ?: return
        val target = state.value.linked ?: return
        if (linkJob?.isActive == true) return

        linkJob = launchGuarded(onError = ::logUnexpectedError) {
            val isUndone = applyLinkUpdates(
                vaultPath,
                listOf(rewrite.reversed())
            ).updated.isNotEmpty()

            if (isUndone) {
                graph = graph?.withoutLink(
                    one = rewrite.path,
                    other = target
                )

                emit(effect = GraphEffect.NoteRewritten(path = rewrite.path))
            }

            lastLink = null

            change {
                it.copy(
                    linked = null,
                    linkFailed = !isUndone
                )
            }
        }
    }

    private fun toggleFolder(folder: String) {
        change {
            val excluded = it.excludedFolders

            val isExcluded = folder in excluded

            it.copy(excludedFolders = if (isExcluded) excluded - folder else excluded + folder)
        }

        launchGuarded(onError = ::logUnexpectedError) {
            val preferences = GraphPreferences(excludedFolders = state.value.excludedFolders)

            preferencesStore.write(
                vaultPath = vaultPath,
                preferences = preferences
            )
        }
    }

    private fun change(transform: (GraphState) -> GraphState) {
        reduce(transform)
        refresh()
    }

    /** Draws the graph for the current state again, replacing any work still running; with its tab closed, nothing. */
    private fun refresh() {
        if (!isShown) return
        previewJob?.cancel()
        refreshJob?.cancel()

        refreshJob = launchGuarded(onError = ::logUnexpectedError) {
            reduce { it.copy(isLoading = graph == null) }

            val whole = graph ?: buildNoteGraph(vaultPath, notes.values).also { graph = it }

            val current = state.value

            val folders = whole
                .nodes
                .map { it.folder }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()

            val shown = whole.withoutFolders(folders = current.excludedFolders)

            val center = current.center?.takeIf {
                it in notes
            }

            val (dots, lines) = when {
                current.scope == GraphScope.WHOLE_VAULT -> vaultOf(shown)
                center != null -> aroundOf(shown, center)
                else -> emptyList<GraphDot>() to emptyList()
            }

            reduce {
                withMatches(
                    state = it.copy(
                        isLoading = false,
                        center = center,
                        dots = dots,
                        lines = lines,
                        folders = folders,
                        isTooLarge =
                        current.scope == GraphScope.WHOLE_VAULT && dots.size > MAX_VAULT_NOTES
                    )
                )
            }

            showPreview()
        }
    }

    private suspend fun vaultOf(graph: NoteGraph): Pair<List<GraphDot>, List<GraphLine>> {
        val shown = if (state.value.showsOrphans) graph else graph.withoutOrphans()

        val points = withContext(context = dispatchers.default) {
            layout.spread(
                graph = shown,
                onRound = { coroutineContext.ensureActive() }
            )
        }

        return dotsOf(
            graph = shown,
            points = points,
            depths = emptyMap()
        ) to linesOf(graph = shown)
    }

    /** The notes around [center], on rings; those close in meaning go on the ring outside the last. */
    private suspend fun aroundOf(
        graph: NoteGraph,
        center: RelativePath
    ): Pair<List<GraphDot>, List<GraphLine>> {
        val depth = state.value.depth

        val depths = graph.depthsFrom(
            center = center,
            depth = depth
        ).toMutableMap()

        val known = graph
            .nodes
            .mapTo(destination = HashSet()) { it.path }

        val isAsked = state.value.showsRelated && state.value.isIndexReady

        val related = if (isAsked) {
            relatedNotes.of(center, notes).filter {
                it in known
            }
        } else {
            emptyList()
        }

        related.forEach {
            depths
                .getOrPut(key = it) {
                    depth + 1
                }
        }

        val around = graph.only(paths = depths.keys).withRelated(
            center = center,
            related = related
        )

        val points = withContext(context = dispatchers.default) {
            layout.rings(
                graph = around,
                depths = depths
            )
        }

        return dotsOf(
            graph = around,
            points = points,
            depths = depths
        ) to linesOf(graph = around)
    }

    private fun linesOf(graph: NoteGraph): List<GraphLine> = graph.edges.map {
        GraphLine(it.first, it.second, it.isRelated)
    }

    private fun showPreviewNow() {
        previewJob?.cancel()

        previewJob = launchGuarded(onError = ::logUnexpectedError) { showPreview() }
    }

    /** The selected note, or the center, in the inspector at once; what it could link to follows, from the model. */
    private suspend fun showPreview() {
        val current = state.value
        val path = current.selected ?: current.center

        val entry = path?.let {
            notes[it]
        } ?: return reduce { it.copy(preview = null) }

        val kept = current
            .preview
            ?.takeIf {
                it.path == path
            }
            ?.suggested
            .orEmpty()

        val preview = previews.of(
            entry = entry,
            lines = current.lines,
            suggested = kept
        )

        reduce {
            if ((it.selected ?: it.center) == path) it.copy(preview = preview) else it
        }

        val shown = graph?.withoutFolders(folders = current.excludedFolders)
        if (shown == null || !current.isIndexReady) return

        val suggested = relatedNotes.notLinked(
            note = path,
            graph = shown,
            notes = notes
        )

        reduce { state ->
            state.copy(
                preview = state.preview?.let {
                    if (it.path == path) it.copy(suggested = suggested) else it
                }
            )
        }
    }

    private fun logUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false) }
    }

    private companion object {
        const val LOG_TAG = "GraphViewModel"
        const val MAX_VAULT_NOTES = 2_000
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the graph"
    }
}
