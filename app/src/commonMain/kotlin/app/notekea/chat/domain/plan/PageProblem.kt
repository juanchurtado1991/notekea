package app.notekea.chat.domain.plan

/** Why a web page could not be clipped from the chat (4.4), in the words the chat says it. */
enum class PageProblem {
    /** Not an http or https address written in the request. */
    BAD_ADDRESS,

    /** An address on this computer or its local network, never fetched. */
    LOCAL_ADDRESS,
    OFFLINE,
    TOO_SLOW,
    TOO_LARGE,

    /** The site refused it: an error page, a login or a paywall. */
    REFUSED,

    /** No article in it: a page that needs JavaScript, a home page, a file. */
    NOT_ARTICLE
}
