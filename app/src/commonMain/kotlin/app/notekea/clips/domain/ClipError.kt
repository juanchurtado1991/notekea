package app.notekea.clips.domain

/** Why a page could not be clipped, in the plain words the chat and the clip dialog use; no note is left behind. */
sealed interface ClipError {
    /** Not an http or https address. */
    data object BadAddress : ClipError

    /** An address on this computer or its local network: never fetched (`GUIDELINES.md` §13). */
    data object LocalAddress : ClipError

    data object Offline : ClipError

    data object TooSlow : ClipError

    /** Larger than a web article should be. */
    data object TooLarge : ClipError

    /** The site answered with an error, such as 404 or 403 (a login or a paywall). */
    data class Refused(val status: Int) : ClipError

    /** No article could be found in it: a page that needs JavaScript, a login wall, a home page, a file. */
    data object NotArticle : ClipError
}
