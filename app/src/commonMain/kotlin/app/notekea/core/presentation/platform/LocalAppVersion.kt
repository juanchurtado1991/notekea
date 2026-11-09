package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The app's version, as About shows it; the window provides the packaged one, previews and tests show `PREVIEW`. */
val LocalAppVersion = staticCompositionLocalOf { AppVersionDefaults.PREVIEW }
