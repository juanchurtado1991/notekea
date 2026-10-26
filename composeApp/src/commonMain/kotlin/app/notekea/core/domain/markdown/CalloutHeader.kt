package app.notekea.core.domain.markdown

/**
 * The first line of a callout (`> [!tip]- Title`): its [kind], the [title] to show (the one written, or the type as
 * written, capitalized, when there is none) and how it [fold]s.
 */
data class CalloutHeader(
    val kind: CalloutKind,
    val title: String,
    val fold: CalloutFold
)
