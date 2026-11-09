package app.notekea.core.domain.markdown

/** Whether a callout can be folded: not at all, or folded with `-` (shown closed) or `+` (shown open) after its type. */
enum class CalloutFold {
    NONE,
    EXPANDED,
    COLLAPSED
}
