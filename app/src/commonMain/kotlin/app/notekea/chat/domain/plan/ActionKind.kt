package app.notekea.chat.domain.plan

/**
 * What the chat can do for a request (3.9), as the model names it ([written]); [needs] are the fields a step fills.
 * Showing is done at once; writing ([isWriting]) or organizing ([isOrganizing]) is confirmed first, on one card; a
 * step [isComposed] has its text written out before the card.
 */
enum class ActionKind(
    val written: String,
    val needs: Set<ActionField> = emptySet(),
    val isWriting: Boolean = false,
    val isOrganizing: Boolean = false,
    val isComposed: Boolean = false,
    val isMemory: Boolean = false,
    // A change to the note as a whole (its properties, tags, name or place), to none of the entries in it.
    val isOnNote: Boolean = false
) {
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
        isWriting = true,
        isComposed = true
    ),
    WRITE_FROM_NOTES(
        written = "write_from_notes",
        needs = setOf(ActionField.TITLE, ActionField.TEXT),
        isWriting = true,
        isComposed = true
    ),

    // Study cards made from a note and added to it (4.6); the open note when none is named.
    MAKE_CARDS(
        written = "make_cards",
        isWriting = true,
        isComposed = true
    ),

    // A web page the request names, clipped into Web sources with notes about it (4.4).
    CLIP_URL(
        written = "clip_url",
        needs = setOf(ActionField.URL),
        isWriting = true,
        isComposed = true
    ),

    // A long text pasted with the request becomes a folder of linked notes, with an index note (decided 2026-11-10).
    NOTES_FROM_TEXT(
        written = "notes_from_text",
        needs = setOf(ActionField.FOLDER),
        isWriting = true,
        isComposed = true
    ),
    APPEND_TEXT(
        written = "append_text",
        needs = setOf(ActionField.TEXT),
        isWriting = true
    ),
    SET_PROPERTY(
        written = "set_property",
        needs = setOf(ActionField.PROPERTY, ActionField.VALUE),
        isWriting = true,
        isOnNote = true
    ),
    ADD_TAG(
        written = "add_tag",
        needs = setOf(ActionField.TAG),
        isWriting = true,
        isOnNote = true
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
    CHANGE_LINE(
        written = "change_line",
        needs = setOf(ActionField.NOTE, ActionField.TEXT, ActionField.LINE),
        isWriting = true
    ),
    LINK_NOTES(
        written = "link_notes",
        needs = setOf(ActionField.NOTE, ActionField.TARGET),
        isWriting = true
    ),
    BOOKMARK(
        written = "bookmark",
        isWriting = true,
        isOnNote = true
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
        isOrganizing = true,
        isOnNote = true
    ),
    MOVE(
        written = "move",
        needs = setOf(ActionField.TARGET),
        isOrganizing = true,
        isOnNote = true
    ),
    TRASH(
        written = "trash",
        isOrganizing = true,
        isOnNote = true
    ),

    // A line of the chat's memory written or taken out (docs/agent-memory.md).
    REMEMBER(
        written = "remember",
        needs = setOf(ActionField.TEXT),
        isWriting = true,
        isMemory = true
    ),
    FORGET(
        written = "forget",
        needs = setOf(ActionField.TEXT),
        isWriting = true,
        isMemory = true
    );

    /** Whether a step of this kind changes the vault, so the plan waits for the card's Do it. */
    val isConfirmed: Boolean get() = isWriting || isOrganizing
}
