package app.notekea.chat.domain

/**
 * What the chat can do for a request (3.9), as the model names it ([written]); the app carries it out, the model only
 * chooses. [needs] are the fields a step of it must fill. Showing something is done at once; writing ([isWriting]) or
 * organizing notes ([isOrganizing]) is always confirmed first, on one card for the whole plan.
 */
enum class ActionKind(
    val written: String,
    val needs: Set<ActionField> = emptySet(),
    val isWriting: Boolean = false,
    val isOrganizing: Boolean = false
) {
    // Asked of the notes: 3.2's answer, a list of notes by their properties or the tasks (3.8).
    ANSWER(written = "answer"),
    FIND_NOTES(
        written = "find_notes",
        needs = setOf(ActionField.REQUEST)
    ),
    SHOW_TASKS(written = "show_tasks"),
    LIST_UNTAGGED(written = "list_untagged"),
    LIST_BROKEN_LINKS(written = "list_broken_links"),

    // Shown at once: nothing changes in the notes.
    OPEN_NOTE(
        written = "open_note",
        needs = setOf(ActionField.NOTE)
    ),
    OPEN_TODAY(written = "open_today"),
    SEARCH(
        written = "search",
        needs = setOf(ActionField.TEXT)
    ),
    SHOW_PANEL(
        written = "show_panel",
        needs = setOf(ActionField.PANEL)
    ),
    SPLIT_PANE(written = "split_pane"),
    CLOSE_OTHER_TABS(written = "close_other_tabs"),
    TOGGLE_SOURCE(written = "toggle_source"),
    SET_THEME(
        written = "set_theme",
        needs = setOf(ActionField.THEME)
    ),

    // Written into the notes, confirmed first.
    CREATE_NOTE(
        written = "create_note",
        needs = setOf(ActionField.TITLE),
        isWriting = true
    ),
    WRITE_FROM_NOTES(
        written = "write_from_notes",
        needs = setOf(ActionField.TITLE, ActionField.TEXT),
        isWriting = true
    ),

    // A long text pasted with the request becomes a folder of linked notes, with an index note (decided 2026-11-10).
    NOTES_FROM_TEXT(
        written = "notes_from_text",
        needs = setOf(ActionField.FOLDER),
        isWriting = true
    ),
    APPEND_TEXT(
        written = "append_text",
        needs = setOf(ActionField.TEXT),
        isWriting = true
    ),
    SET_PROPERTY(
        written = "set_property",
        needs = setOf(ActionField.PROPERTY, ActionField.VALUE),
        isWriting = true
    ),
    ADD_TAG(
        written = "add_tag",
        needs = setOf(ActionField.TAG),
        isWriting = true
    ),
    ADD_TASK(
        written = "add_task",
        needs = setOf(ActionField.TEXT),
        isWriting = true
    ),
    COMPLETE_TASK(
        written = "complete_task",
        needs = setOf(ActionField.NOTE, ActionField.TEXT),
        isWriting = true
    ),
    LINK_NOTES(
        written = "link_notes",
        needs = setOf(ActionField.NOTE, ActionField.TARGET),
        isWriting = true
    ),
    BOOKMARK(
        written = "bookmark",
        isWriting = true
    ),

    // Organizing, confirmed first with every note listed.
    CREATE_FOLDER(
        written = "create_folder",
        needs = setOf(ActionField.FOLDER),
        isOrganizing = true
    ),
    RENAME(
        written = "rename",
        needs = setOf(ActionField.TITLE),
        isOrganizing = true
    ),
    MOVE(
        written = "move",
        needs = setOf(ActionField.TARGET),
        isOrganizing = true
    ),
    TRASH(
        written = "trash",
        isOrganizing = true
    ),

    // Asked for something the chat never does (deleting for good, Settings, outside the vault, code).
    REFUSE(written = "refuse");

    /** Whether a step of this kind changes the vault, so the plan waits for the card's Do it. */
    val isConfirmed: Boolean get() = isWriting || isOrganizing

    companion object {
        fun of(written: String): ActionKind? = entries.firstOrNull {
            it.written == written
        }
    }
}
