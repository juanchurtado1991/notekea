package app.notekea.core.presentation.theme.palette

import androidx.compose.ui.graphics.Color
import app.notekea.core.appearance.domain.ColorTheme

/** The two tones a color theme is made from (`ROADMAP.md` 2.12): a [deep] one and a [light] one. */
internal data class ThemeTones(
    val deep: Color,
    val light: Color
) {
    companion object {
        /** The tones of every theme but [ColorTheme.DEFAULT], which has its own palette ([NotekeaPalette]). */
        val all: Map<ColorTheme, ThemeTones> = mapOf(
            ColorTheme.MIDNIGHT_LIME to ThemeTones(deep = Color(color = 0xFF071A52), light = Color(color = 0xFFA7FF83)),
            ColorTheme.DEEP_SAGE to ThemeTones(deep = Color(color = 0xFF152D35), light = Color(color = 0xFFD4ECDD)),
            ColorTheme.SIGNAL_ALMOND to ThemeTones(deep = Color(color = 0xFFF62440), light = Color(color = 0xFFFFFAF3)),
            ColorTheme.COSMIC_VIOLET to ThemeTones(deep = Color(color = 0xFF0C134F), light = Color(color = 0xFFD4ADFC)),
            ColorTheme.AZURE_CLOUD to ThemeTones(deep = Color(color = 0xFF1B4EF5), light = Color(color = 0xFFF7DCFF)),
            ColorTheme.NEON_PORCELAIN to ThemeTones(
                deep = Color(color = 0xFFFF6115),
                light = Color(color = 0xFFFFFCF4)
            ),
            ColorTheme.FOREST_MINT to ThemeTones(deep = Color(color = 0xFF18251D), light = Color(color = 0xFFB7FF72)),
            ColorTheme.PROMPT_GREEN to ThemeTones(deep = Color(color = 0xFF0B132B), light = Color(color = 0xFF39FF88)),
            ColorTheme.CLAY_BUTTER to ThemeTones(deep = Color(color = 0xFF6B352A), light = Color(color = 0xFFFFF1A6))
        )
    }
}
