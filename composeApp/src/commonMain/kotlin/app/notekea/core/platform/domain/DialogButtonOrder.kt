package app.notekea.core.platform.domain

/** Order of the confirmation buttons in dialogs: macOS and GNOME put Cancel first, Windows puts it last. */
enum class DialogButtonOrder {
    CANCEL_THEN_PRIMARY,
    PRIMARY_THEN_CANCEL
}
