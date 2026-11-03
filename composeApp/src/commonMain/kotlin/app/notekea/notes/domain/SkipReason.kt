package app.notekea.notes.domain

/** Why a note is not in the index. */
enum class SkipReason {
    /** Larger than [IndexVaultUseCase.MAX_NOTE_BYTES]: an export or a log, not a note to search. */
    TOO_LARGE,

    /** It could not be read (permissions, a file in use). */
    UNREADABLE,

    /** A chunk was longer than the embedding model reads. */
    TOO_LONG
}
