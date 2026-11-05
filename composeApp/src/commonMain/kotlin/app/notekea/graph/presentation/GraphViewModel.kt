package app.notekea.graph.presentation

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.domain.Outcome
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
import app.notekea.graph.domain.GraphNode
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
 * The graph tab (`ROADMAP.md` 2.7) for the vault at [vaultPath]. It opens small, on the note it was opened from and its
 * neighbors, on rings; the whole vault is a choice, or what shows with no note. Choosing a note around another moves it
 * to the center. Every change replaces the work under way, and the layout runs off the UI thread.
 */
class GraphViewModel(
    private val vaultPath: String,
    private val buildNoteGraph: BuildNoteGraphUseCase,
    private val findRelatedNotes: FindRelatedNotesUseCase,
    private val linkNotes: LinkNotesUseCase,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val fuzzyMatcher: FuzzyMatcher,
    private val noteTextCache: NoteTextCache,
    private val markdownLineStripper: MarkdownLineStripper,
    private val preferencesStore: GraphPreferencesStore,
    private val dispatchers: AppDispatchers
) : BaseViewModel<GraphState, GraphIntent, GraphEffect>(initialState = GraphState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val layout = GraphLayout()
    private var notes: Map<RelativePath, NoteEntry> = emptyMap()
    private var graph: NoteGraph? = null
    private var openedFrom: RelativePath? = null
    private var refreshJob: Job? = null
    private var lastLink: NoteRewrite? = null
    private var related: Pair<RelativePath, List<RelativePath>>? = null

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            val preferences = preferencesStore.read(vaultPath = vaultPath)
            reduce { it.copy(excludedFolders = preferences.excludedFolders) }
            refresh()
        }
    }

    override fun handleIntent(
        intent: GraphIntent
    ) {
        when (intent) {
            is GraphIntent.OnNotesChange -> changeNotes(notes = intent.notes)
            is GraphIntent.OnIndexChange -> changeIndex(isReady = intent.isReady)
            is GraphIntent.OnOpen -> open(note = intent.note)
            is GraphIntent.OnScopeSelect -> selectScope(scope = intent.scope)
            is GraphIntent.OnDepthSelect -> change { it.copy(depth = intent.depth) }
            is GraphIntent.OnNoteSelect -> select(path = intent.path)
            is GraphIntent.OnMove -> move(direction = intent.direction)
            GraphIntent.OnOpenSelected -> state.value.selected?.let { emit(effect = GraphEffect.OpenNote(path = it)) }
            GraphIntent.OnBack -> openedFrom?.let { emit(effect = GraphEffect.OpenNote(path = it)) }
            GraphIntent.OnInspectorToggle -> reduce { it.copy(isInspectorOpen = !it.isInspectorOpen) }
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

    private fun changeNotes(
        notes: Collection<NoteEntry>
    ) {
        this.notes = notes.associateBy { it.file.path }
        graph = null
        related = null
        refresh()
    }

    private fun changeIndex(
        isReady: Boolean
    ) {
        if (isReady == state.value.isIndexReady) return
        related = null
        change { it.copy(isIndexReady = isReady) }
    }

    private fun open(
        note: RelativePath?
    ) {
        openedFrom = note
        change {
            it.copy(
                center = note,
                selected = note,
                scope = if (note == null) GraphScope.WHOLE_VAULT else GraphScope.THIS_NOTE
            )
        }
    }

    private fun selectScope(
        scope: GraphScope
    ) {
        change {
            val center = it.center ?: it.selected
            val scoped = if (scope == GraphScope.THIS_NOTE && center != null) scope else GraphScope.WHOLE_VAULT
            it.copy(scope = scoped, center = center)
        }
    }

    private fun select(
        path: RelativePath
    ) {
        reduce { it.copy(linked = null, linkFailed = false) }
        if (state.value.scope == GraphScope.THIS_NOTE) {
            change { it.copy(center = path, selected = path) }
        } else {
            reduce { it.copy(selected = path) }
            launchGuarded(onError = ::logUnexpectedError) { showPreview() }
        }
    }

    private fun move(
        direction: GraphDirection
    ) {
        val current = state.value
        val selected = current.selected ?: return
        noteTowards(direction, selected, current.dots, current.lines)?.let { select(path = it) }
    }

    private fun closeSearch() {
        reduce { it.copy(isSearching = false, query = "", matches = emptyList(), matchIndex = 0, revealed = null) }
    }

    private fun changeQuery(
        query: String
    ) {
        reduce { withMatches(state = it.copy(query = query, matchIndex = 0, revealed = null)) }
    }

    private fun withMatches(
        state: GraphState
    ): GraphState {
        val whole = graph?.withoutFolders(folders = state.excludedFolders)?.nodes?.map { it.path }.orEmpty()
        val paths = if (state.scope == GraphScope.THIS_NOTE) whole else state.dots.map { it.path }
        val matches = matchesOf(query = state.query, paths = paths, matcher = fuzzyMatcher)
        val lastIndex = matches.lastIndex.coerceAtLeast(minimumValue = 0)
        return state.copy(matches = matches, matchIndex = state.matchIndex.coerceAtMost(maximumValue = lastIndex))
    }

    private fun stepMatch(
        isForward: Boolean
    ) {
        val current = state.value
        val count = current.matches.size
        if (count == 0) return
        val index = when {
            current.revealed != null -> (current.matchIndex + if (isForward) 1 else count - 1) % count
            isForward -> 0
            else -> count - 1
        }
        val path = current.matches[index]
        reduce { it.copy(matchIndex = index, revealed = path) }
        select(path = path)
    }

    private fun link(
        target: RelativePath
    ) {
        val source = state.value.preview?.path ?: return
        launchGuarded(onError = ::logUnexpectedError) {
            lastLink = linkNotes(vaultPath, source, target, notes.keys)
            val isLinked = lastLink != null
            if (isLinked) graph = graph?.withLink(one = source, other = target)
            change { it.copy(linked = target.takeIf { isLinked }, linkFailed = !isLinked) }
        }
    }

    private fun undoLink() {
        val rewrite = lastLink ?: return
        val target = state.value.linked ?: return
        launchGuarded(onError = ::logUnexpectedError) {
            val isUndone = applyLinkUpdates(vaultPath, listOf(rewrite.reversed())).updated.isNotEmpty()
            if (isUndone) graph = graph?.withoutLink(one = rewrite.path, other = target)
            lastLink = null
            change { it.copy(linked = null, linkFailed = !isUndone) }
        }
    }

    private fun toggleFolder(
        folder: String
    ) {
        change {
            val excluded = it.excludedFolders
            it.copy(excludedFolders = if (folder in excluded) excluded - folder else excluded + folder)
        }
        launchGuarded(onError = ::logUnexpectedError) {
            val preferences = GraphPreferences(excludedFolders = state.value.excludedFolders)
            preferencesStore.write(vaultPath = vaultPath, preferences = preferences)
        }
    }

    private fun change(
        transform: (GraphState) -> GraphState
    ) {
        reduce(transform)
        refresh()
    }

    /** Draws the graph for the current state again, replacing any work still running. */
    private fun refresh() {
        refreshJob?.cancel()
        refreshJob = launchGuarded(onError = ::logUnexpectedError) {
            reduce { it.copy(isLoading = graph == null) }
            val whole = graph ?: buildNoteGraph(vaultPath, notes.values).also { graph = it }
            val current = state.value
            val folders = whole.nodes.map { it.folder }.filter { it.isNotEmpty() }.distinct().sorted()
            val shown = whole.withoutFolders(folders = current.excludedFolders)
            val center = current.center?.takeIf { it in notes && current.scope == GraphScope.THIS_NOTE }
            val (dots, lines) = if (center == null) vaultOf(shown) else aroundOf(shown, center)
            reduce {
                withMatches(
                    state = it.copy(
                        isLoading = false,
                        dots = dots,
                        lines = lines,
                        folders = folders,
                        isTooLarge = center == null && dots.size > MAX_VAULT_NOTES
                    )
                )
            }
            showPreview()
        }
    }

    private suspend fun vaultOf(
        graph: NoteGraph
    ): Pair<List<GraphDot>, List<GraphLine>> {
        val shown = if (state.value.showsOrphans) graph else graph.withoutOrphans()
        val points = withContext(context = dispatchers.default) {
            layout.spread(graph = shown, onRound = { coroutineContext.ensureActive() })
        }
        return dotsOf(graph = shown, points = points, depths = emptyMap()) to linesOf(graph = shown)
    }

    /** The notes around [center], on rings; those close in meaning go on the ring outside the last. */
    private suspend fun aroundOf(
        graph: NoteGraph,
        center: RelativePath
    ): Pair<List<GraphDot>, List<GraphLine>> {
        val depth = state.value.depth
        val depths = graph.depthsFrom(center = center, depth = depth).toMutableMap()
        val known = graph.nodes.mapTo(destination = HashSet()) { it.path }
        val related = if (state.value.showsRelated) relatedTo(note = center).filter { it in known } else emptyList()
        related.forEach { depths.getOrPut(key = it) { depth + 1 } }
        val around = graph.only(paths = depths.keys).withRelated(center = center, related = related)
        val points = withContext(context = dispatchers.default) { layout.rings(graph = around, depths = depths) }
        return dotsOf(graph = around, points = points, depths = depths) to linesOf(graph = around)
    }

    /** The notes close in meaning to [note], remembered, as the map and the inspector both ask. */
    private suspend fun relatedTo(
        note: RelativePath
    ): List<RelativePath> {
        related?.takeIf { it.first == note }?.let { return it.second }
        val entry = notes[note]
        if (!state.value.isIndexReady || entry == null) return emptyList()
        val text = noteTextCache.textsOf(vaultPath = vaultPath, notes = listOf(entry))[note]?.body
            ?: return emptyList()
        val found = findRelatedNotes(vaultPath, note, text, notes.keys) as? Outcome.Success ?: return emptyList()
        val close = found.value.related.filter { it.score >= MIN_RELATED_CLOSENESS }.map { it.path }
        related = note to close
        return close
    }

    private fun linesOf(
        graph: NoteGraph
    ): List<GraphLine> = graph.edges.map { GraphLine(it.first, it.second, it.isRelated) }

    /** The selected note, or the center, in the inspector. */
    private suspend fun showPreview() {
        val current = state.value
        val path = current.selected ?: current.center
        val entry = path?.let { notes[it] }
        if (path == null || entry == null) return reduce { it.copy(preview = null) }
        val body = noteTextCache.textsOf(vaultPath = vaultPath, notes = listOf(entry))[path]?.body.orEmpty()
        val joined = graph?.neighbors()?.get(path).orEmpty().toSet()
        val preview = GraphPreview(
            path = path,
            name = path.nameWithoutExtension,
            folder = GraphNode(path = path, links = 0).folder,
            snippet = snippetOf(body = body),
            connected = current.lines.filter { !it.isRelated }.mapNotNull {
                when (path) {
                    it.first -> it.second
                    it.second -> it.first
                    else -> null
                }
            },
            suggested = relatedTo(note = path).filter { it !in joined }
        )
        reduce { it.copy(preview = preview) }
    }

    private fun snippetOf(
        body: String
    ): String = body.lineSequence()
        .filter { it.isNotBlank() && !it.startsWith(prefix = HEADING_MARK) }
        .take(n = SNIPPET_LINES)
        .joinToString(separator = " ") { markdownLineStripper.strip(line = it, ranges = emptyList()).text }
        .take(n = MAX_SNIPPET)

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false) }
    }

    private companion object {
        const val HEADING_MARK = "#"
        const val LOG_TAG = "GraphViewModel"
        const val MAX_SNIPPET = 220
        const val MAX_VAULT_NOTES = 2_000
        const val MIN_RELATED_CLOSENESS = 0.82f
        const val SNIPPET_LINES = 4
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the graph"
    }
}
