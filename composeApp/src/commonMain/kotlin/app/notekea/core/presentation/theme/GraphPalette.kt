package app.notekea.core.presentation.theme

import androidx.compose.ui.graphics.Color

/** The colors of the graph's folders (`ROADMAP.md` 2.7): mid tones that read on light and dark surfaces alike. */
object GraphPalette {
    private val colors = listOf(
        Color(color = 0xFF1D9E75),
        Color(color = 0xFFD85A30),
        Color(color = 0xFF378ADD),
        Color(color = 0xFF7F77DD),
        Color(color = 0xFFBA7517),
        Color(color = 0xFFD4537E),
        Color(color = 0xFF639922),
        Color(color = 0xFF0F8FA8)
    )

    /** The color of the folder at [index], repeating when a vault has more folders than colors. */
    fun colorAt(
        index: Int
    ): Color = colors[index.mod(other = colors.size)]
}
