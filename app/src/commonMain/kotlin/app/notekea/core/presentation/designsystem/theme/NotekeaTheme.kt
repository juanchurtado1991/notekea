package app.notekea.core.presentation.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode

/** Code blocks read their style from here; set by [NotekeaTheme]. */
val LocalNotekeaCodeTextStyle = staticCompositionLocalOf { TextStyle.Default }

/**
 * Root theme of the app: the color [theme] chosen in Settings → Appearance ([LocalAppearance]) in light or dark as its
 * mode says (System follows the system's setting, [LocalSystemDarkTheme] where Compose cannot read it), bundled
 * typography, 6 dp control and 10 dp card corners. Every screen and the component gallery render inside it.
 */
@Composable
fun NotekeaTheme(
    darkTheme: Boolean = isDark(mode = LocalAppearance.current.mode),
    theme: ColorTheme = LocalAppearance.current.theme,
    content: @Composable () -> Unit
) {
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(size = NotekeaDimensions.ControlRadius),
        small = RoundedCornerShape(size = NotekeaDimensions.ControlRadius),
        medium = RoundedCornerShape(size = NotekeaDimensions.CardRadius),
        large = RoundedCornerShape(size = NotekeaDimensions.CardRadius)
    )

    MaterialTheme(
        colorScheme = ColorThemeSchemes.of(
            theme = theme,
            darkTheme = darkTheme
        ),
        typography = notekeaTypography(),
        shapes = shapes
    ) {
        CompositionLocalProvider(
            // A text with no style of its own is interface text, 13 as on macOS (3A); a note's text names bodyLarge.
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
            LocalNotekeaCodeTextStyle provides notekeaCodeTextStyle(),
            LocalReduceMotion provides
                (LocalAppearance.current.reduceMotion || LocalSystemReduceMotion.current)
        ) { content() }
    }
}

/** Whether [mode] draws the app dark: always or never, or as the system is set. */
@Composable
private fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> LocalSystemDarkTheme.current ?: isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
