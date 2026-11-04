package app.notekea.core.presentation.menu

import org.jetbrains.compose.resources.StringResource

/** One menu of the menu bar: its [title] and its [entries], top to bottom. */
data class AppMenu(
    val title: StringResource,
    val entries: List<AppMenuEntry>
)
