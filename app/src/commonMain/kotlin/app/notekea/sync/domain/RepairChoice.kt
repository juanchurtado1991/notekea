package app.notekea.sync.domain

/** How a vault left in the middle of a merge is put right; the person chooses, nothing is repaired by itself. */
enum class RepairChoice {
    /** The merge is undone and everything written here is kept; the next sync brings the remote's side again. */
    KEEP_MINE,

    /** The vault goes back to what the remote has; what was changed here is copied to the vault's trash first. */
    TAKE_REMOTE
}
