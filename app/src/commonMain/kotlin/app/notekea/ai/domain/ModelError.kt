package app.notekea.ai.domain

/** Why a model could not be installed (`ROADMAP.md` 3.1). */
sealed interface ModelError {
    /** The download stopped: no connection, or the server answered with an error. */
    data object Network : ModelError

    /** The downloaded file is not the one the catalog names; it was deleted. */
    data object ChecksumMismatch : ModelError

    /** The disk is missing [neededBytes] more for the files still to download. */
    data class NotEnoughDisk(
        val neededBytes: Long
    ) : ModelError

    /** The models folder could not be written. */
    data object Storage : ModelError
}
