package app.notekea.core.presentation.designsystem.components

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals

/** A dialog's X is drawn in red, in both themes (the developer's request of 2026-11-14). */
class NotekeaCloseButtonTest {
    private fun hasRed(isDark: Boolean): Boolean {
        val scene = ImageComposeScene(
            width = SIZE,
            height = SIZE,
            density = Density(density = 2f)
        ) {
            NotekeaTheme(darkTheme = isDark) {
                NotekeaCloseButton(
                    label = "Close",
                    onClick = {}
                )
            }
        }

        val bitmap = Bitmap.makeFromImage(image = scene.render())
        scene.close()

        return (0 until SIZE).any { x ->
            (0 until SIZE).any { y ->
                val color = bitmap.getColor(
                    x = x,
                    y = y
                )

                Color.getA(color = color) > OPAQUE &&
                    Color.getR(color = color) > Color.getG(color = color) + REDDER &&
                    Color.getR(color = color) > Color.getB(color = color) + REDDER
            }
        }
    }

    @Test
    fun `the close button is red in the light and the dark theme`() {
        assertEquals(
            expected = listOf(true, true),
            actual = listOf(hasRed(isDark = false), hasRed(isDark = true))
        )
    }

    private companion object {
        const val SIZE = 96
        const val OPAQUE = 200
        const val REDDER = 60
    }
}
