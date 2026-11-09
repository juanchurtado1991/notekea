package app.notekea.core.presentation.designsystem.theme

import androidx.compose.ui.graphics.Color

/** The colors of the graph's folders (`ROADMAP.md` 2.7): mid tones that read on light and dark surfaces alike. */
object GraphPalette {
    private val colors = listOf(
        Color(color = 0xFF5FA88C),
        Color(color = 0xFFC9826A),
        Color(color = 0xFF6E9BD0),
        Color(color = 0xFF9A93D6),
        Color(color = 0xFFBE9552),
        Color(color = 0xFFC97F9B),
        Color(color = 0xFF8CA665),
        Color(color = 0xFF5EA3B0)
    )

    /** The color of the folder at [index], repeating when a vault has more folders than colors. */
    fun colorAt(
        index: Int
    ): Color = colors[index.mod(other = colors.size)]
}
