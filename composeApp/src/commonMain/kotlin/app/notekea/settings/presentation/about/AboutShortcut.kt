package app.notekea.settings.presentation.about

import androidx.compose.runtime.Immutable
import app.notekea.core.platform.domain.Shortcut
import org.jetbrains.compose.resources.StringResource

/** One row of About's shortcuts: what it does ([label]) and its [shortcut] on this platform. */
@Immutable
data class AboutShortcut(
    val label: StringResource,
    val shortcut: Shortcut
)
