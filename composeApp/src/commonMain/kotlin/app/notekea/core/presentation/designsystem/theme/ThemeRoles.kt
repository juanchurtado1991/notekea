package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** The name of an entry that is gone: as readable as any muted text, so what says it is gone is not its color. */
val ColorScheme.missingEntry: Color
    get() = onSurfaceVariant
