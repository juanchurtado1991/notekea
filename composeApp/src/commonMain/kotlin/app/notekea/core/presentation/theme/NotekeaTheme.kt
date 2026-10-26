package app.notekea.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/** Code blocks read their style from here; set by [NotekeaTheme]. */
val LocalNotekeaCodeTextStyle = staticCompositionLocalOf { TextStyle.Default }

/**
 * Root theme of the app: kea color scheme (following the system light/dark setting by default, [LocalSystemDarkTheme]
 * where Compose cannot read it), bundled typography,
 * 6 dp control and 10 dp card corners. Every screen and the component gallery render inside it.
 */
@Composable
fun NotekeaTheme(
    darkTheme: Boolean = LocalSystemDarkTheme.current ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(size = NotekeaDimensions.ControlRadius),
        small = RoundedCornerShape(size = NotekeaDimensions.ControlRadius),
        medium = RoundedCornerShape(size = NotekeaDimensions.CardRadius),
        large = RoundedCornerShape(size = NotekeaDimensions.CardRadius)
    )
    MaterialTheme(
        colorScheme = NotekeaColorSchemes.forTheme(darkTheme = darkTheme),
        typography = notekeaTypography(),
        shapes = shapes
    ) {
        CompositionLocalProvider(LocalNotekeaCodeTextStyle provides notekeaCodeTextStyle()) {
            content()
        }
    }
}
