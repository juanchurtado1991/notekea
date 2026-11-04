package app.notekea.core.presentation.menu

import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.presentation.platform.WindowDialog
import org.jetbrains.compose.resources.StringResource

/** A line of an [AppMenu]. */
sealed interface AppMenuEntry {
    /** Does what [shortcut] does on the screen shown now; disabled when that screen does not offer it. */
    data class Command(val label: StringResource, val shortcut: Shortcut) : AppMenuEntry

    /** Opens [dialog] over the window; it has no shortcut. */
    data class OpensDialog(val label: StringResource, val dialog: WindowDialog) : AppMenuEntry

    data object Separator : AppMenuEntry
}
