package app.notekea.sync.domain

/** What to keep of a note changed on both sides. */
enum class ConflictChoice {
    /** Ours, the copy written beside the note, replaces the remote's version. */
    MINE,

    /** The remote's version stays; ours is removed. */
    THEIRS,

    /** Both stay: ours under its own name, "Note (mine).md". */
    BOTH
}
