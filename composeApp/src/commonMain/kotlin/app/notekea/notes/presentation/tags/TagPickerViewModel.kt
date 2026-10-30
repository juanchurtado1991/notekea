package app.notekea.notes.presentation.tags

import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.TagSearch

/**
 * The tag picker (`ROADMAP.md` 1.17), opened by the toolbar's Tag button: a search over the vault's tags, in the
 * frontmatter or in the text, the most used shown first. Typing filters them, and a name the vault lacks is offered
 * as a new tag; once it is written in a note and saved, the vault's tags include it. The tags come from the vault tree
 * ([TagPickerIntent.OnTagsChange]), so the picker never reads the vault itself.
 */
class TagPickerViewModel : BaseViewModel<TagPickerState, TagPickerIntent, TagPickerEffect>(
    initialState = TagPickerState()
) {
    private val tagSearch = TagSearch()
    private var tags: List<String> = emptyList()

    override fun handleIntent(
        intent: TagPickerIntent
    ) {
        when (intent) {
            is TagPickerIntent.OnOpen -> reduce { rowsFor(state = TagPickerState(isOpen = true, query = intent.query)) }
            TagPickerIntent.OnClose -> close(effect = TagPickerEffect.Dismissed)
            TagPickerIntent.OnConfirm -> choose(index = state.value.selectedIndex)
            is TagPickerIntent.OnQueryChange -> reduce { rowsFor(state = it.copy(query = intent.query)) }
            is TagPickerIntent.OnRowClick -> choose(index = intent.index)
            TagPickerIntent.OnSelectNext -> reduce { moved(state = it, step = 1) }
            TagPickerIntent.OnSelectPrevious -> reduce { moved(state = it, step = -1) }
            is TagPickerIntent.OnTagsChange -> changeTags(tags = intent.tags)
        }
    }

    private fun changeTags(
        tags: List<String>
    ) {
        this.tags = tags
        if (state.value.isOpen) reduce { rowsFor(state = it) }
    }

    private fun choose(
        index: Int
    ) {
        val row = state.value.rows.getOrNull(index = index) ?: return
        close(effect = TagPickerEffect.TagChosen(name = row.name))
    }

    private fun close(
        effect: TagPickerEffect
    ) {
        reduce { TagPickerState() }
        emit(effect = effect)
    }

    /** The rows for the state's query: matching tags, then the query as a new tag; the first one selected. */
    private fun rowsFor(
        state: TagPickerState
    ): TagPickerState {
        val limit = if (state.query.isBlank()) SUGGESTED_TAGS else MATCHING_TAGS
        val found = tagSearch.search(tags = tags, query = state.query, limit = limit).map { TagPickerRow(name = it) }
        val new = tagSearch.newTag(tags = tags, query = state.query)?.let { TagPickerRow(name = it, isNew = true) }
        return state.copy(rows = found + listOfNotNull(new), selectedIndex = 0)
    }

    /** Moves the selection by [step], wrapping around at both ends. */
    private fun moved(
        state: TagPickerState,
        step: Int
    ): TagPickerState {
        if (state.rows.isEmpty()) return state
        return state.copy(selectedIndex = (state.selectedIndex + step).mod(other = state.rows.size))
    }

    companion object {
        private const val MATCHING_TAGS = 20
        private const val SUGGESTED_TAGS = 8
    }
}
