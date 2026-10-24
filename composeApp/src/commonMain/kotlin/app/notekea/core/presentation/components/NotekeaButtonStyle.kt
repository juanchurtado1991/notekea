package app.notekea.core.presentation.components

/**
 * Visual weight of a [NotekeaButton]: one primary action per area, destructive actions always named by a verb, and
 * [TEXT] for low-emphasis actions that sit inside content, such as removing a row.
 */
enum class NotekeaButtonStyle {
    DESTRUCTIVE,
    PRIMARY,
    SECONDARY,
    TEXT
}
