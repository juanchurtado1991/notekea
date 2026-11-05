package app.notekea.graph.presentation

import app.notekea.core.notes.domain.RelativePath

/**
 * State of the graph tab (`ROADMAP.md` 2.7). [scope] and [depth] say what [dots] and [lines] hold, around [center];
 * [selected] is the note the inspector shows as [preview], open while [isInspectorOpen]. [folders] are the vault's
 * top-level folders, which color the dots and the legend, and [excludedFolders] those left out. [showsRelated] adds the
 * dashed edges, which need the index ([isIndexReady]); [showsOrphans] the notes with no edge, in the whole vault.
 * [isTooLarge] suggests going back to one note. While [isSearching], [matches] are the notes named like [query], best
 * first: those on the map, or any note around one, as a match moves there. [matchIndex] is the one [revealed], which the
 * map brings into view. [linked] is the note just linked to, which can be undone, and [linkFailed] says a note could
 * not be changed.
 */
data class GraphState(
    val isLoading: Boolean = true,
    val scope: GraphScope = GraphScope.WHOLE_VAULT,
    val depth: Int = 1,
    val center: RelativePath? = null,
    val dots: List<GraphDot> = emptyList(),
    val lines: List<GraphLine> = emptyList(),
    val selected: RelativePath? = null,
    val preview: GraphPreview? = null,
    val isInspectorOpen: Boolean = true,
    val folders: List<String> = emptyList(),
    val excludedFolders: Set<String> = emptySet(),
    val showsRelated: Boolean = true,
    val showsOrphans: Boolean = false,
    val isIndexReady: Boolean = true,
    val isTooLarge: Boolean = false,
    val isSearching: Boolean = false,
    val query: String = "",
    val matches: List<RelativePath> = emptyList(),
    val matchIndex: Int = 0,
    val revealed: RelativePath? = null,
    val linked: RelativePath? = null,
    val linkFailed: Boolean = false
) {
    /** A note with nothing to show around it, or a vault with no links: the tab teaches how to link. */
    val isEmpty: Boolean
        get() = !isLoading && lines.isEmpty() && (dots.size <= 1 || !showsOrphans)
}
