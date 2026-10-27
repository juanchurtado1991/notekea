package app.notekea.core.presentation.about

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource

/** A group of About's shortcuts under its [title], most used first. */
@Immutable
data class AboutShortcutGroup(
    val title: StringResource,
    val shortcuts: List<AboutShortcut>
)
