package app.notekea.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.presentation.theme.palette.NotekeaPalette
import app.notekea.core.presentation.theme.palette.ThemeTones

/**
 * The light and dark color schemes of every [ColorTheme] (`ROADMAP.md` 2.12), with the same roles as
 * [NotekeaColorSchemes], which [ColorTheme.DEFAULT] uses as it is. The others are built from their two [ThemeTones]:
 * in dark the deep tone is the page and the light tone leads; in light the light tone is the page (a tint of it when it
 * is vivid) and the deep tone leads. Every text color is made readable on its background ([readableOn], WCAG AA).
 */
object ColorThemeSchemes {
    private val schemes: Map<Pair<ColorTheme, Boolean>, ColorScheme> by lazy {
        ColorTheme.entries.flatMap { theme ->
            listOf(true, false).map { dark -> (theme to dark) to build(theme = theme, darkTheme = dark) }
        }.toMap()
    }

    fun of(
        theme: ColorTheme,
        darkTheme: Boolean
    ): ColorScheme = schemes.getValue(key = theme to darkTheme)

    private fun build(
        theme: ColorTheme,
        darkTheme: Boolean
    ): ColorScheme {
        val tones = ThemeTones.all[theme] ?: return NotekeaColorSchemes.forTheme(darkTheme = darkTheme)
        return if (darkTheme) darkSchemeOf(tones = tones) else lightSchemeOf(tones = tones)
    }

    private fun darkSchemeOf(
        tones: ThemeTones
    ): ColorScheme {
        val isDeep = tones.deep.luminance() < DEEP_LUMINANCE
        val page = mix(tones.deep, Color.Black, if (isDeep) DEEP_PAGE_SHADE else VIVID_PAGE_SHADE)
        val surface = mix(page, Color.White, SURFACE_LIFT)
        val variant = mix(page, Color.White, VARIANT_LIFT)
        val lead = if (isDeep) tones.light else mix(tones.deep, Color.White, VIVID_LEAD_LIFT)
        val primary = readableOn(color = lead, backgrounds = listOf(page, surface))
        val onPrimary = readableOn(color = page, backgrounds = listOf(primary))
        val selection = mix(page, lead, SELECTION_MIX)
        val inkTint = mix(Color.White, tones.light, INK_TINT)
        val ink = readableOn(color = inkTint, backgrounds = listOf(page, variant, selection))
        val muted = readableOn(color = mix(ink, page, MUTED_MIX), backgrounds = listOf(page, surface, variant))
        return darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = selection,
            onPrimaryContainer = ink,
            tertiary = primary,
            onTertiary = onPrimary,
            background = page,
            onBackground = ink,
            surface = surface,
            onSurface = ink,
            surfaceVariant = variant,
            onSurfaceVariant = muted,
            outline = mix(page, Color.White, OUTLINE_LIFT),
            outlineVariant = mix(page, Color.White, OUTLINE_VARIANT_LIFT),
            error = NotekeaPalette.ErrorDark,
            onError = NotekeaPalette.OnErrorDark
        )
    }

    private fun lightSchemeOf(
        tones: ThemeTones
    ): ColorScheme {
        val isPale = tones.light.luminance() > PALE_LUMINANCE
        val page = if (isPale) tones.light else mix(tones.light, Color.White, VIVID_PAGE_TINT)
        val surface = mix(page, Color.White, LIGHT_SURFACE_MIX)
        val variant = mix(page, tones.deep, LIGHT_VARIANT_MIX)
        val primary = readableOn(color = tones.deep, backgrounds = listOf(page, surface, Color.White))
        val selection = when {
            isPale -> mix(page, tones.deep, PALE_SELECTION_MIX)
            else -> mix(tones.light, Color.White, VIVID_SELECTION_TINT)
        }
        val accent = if (isPale) primary else tones.light
        val inkShade = mix(tones.deep, Color.Black, INK_SHADE)
        val ink = readableOn(color = inkShade, backgrounds = listOf(page, variant, selection))
        val muted = readableOn(color = mix(ink, page, MUTED_MIX), backgrounds = listOf(page, surface, variant))
        return lightColorScheme(
            primary = primary,
            onPrimary = readableOn(color = Color.White, backgrounds = listOf(primary)),
            primaryContainer = selection,
            onPrimaryContainer = readableOn(color = primary, backgrounds = listOf(selection)),
            tertiary = accent,
            onTertiary = readableOn(color = ink, backgrounds = listOf(accent)),
            background = page,
            onBackground = ink,
            surface = surface,
            onSurface = ink,
            surfaceVariant = variant,
            onSurfaceVariant = muted,
            outline = mix(page, tones.deep, LIGHT_OUTLINE_MIX),
            outlineVariant = mix(page, tones.deep, LIGHT_OUTLINE_VARIANT_MIX),
            error = NotekeaPalette.ErrorLight,
            onError = NotekeaPalette.OnErrorLight
        )
    }

    /** [from] moved [fraction] of the way to [to]. */
    private fun mix(
        from: Color,
        to: Color,
        fraction: Float
    ): Color = lerp(start = from, stop = to, fraction = fraction)

    // A deep tone is dark enough to be a dark page as it is (navy, teal); a vivid one (red, orange) only as a shade.
    private const val DEEP_LUMINANCE = 0.06f

    // A pale light tone (almond, porcelain) is a light page as it is; a vivid one (lime, violet) only as a tint.
    private const val PALE_LUMINANCE = 0.83f
    private const val DEEP_PAGE_SHADE = 0.25f
    private const val VIVID_PAGE_SHADE = 0.88f
    private const val VIVID_LEAD_LIFT = 0.3f
    private const val SURFACE_LIFT = 0.04f
    private const val VARIANT_LIFT = 0.08f
    private const val SELECTION_MIX = 0.22f
    private const val INK_TINT = 0.15f
    private const val MUTED_MIX = 0.4f
    private const val OUTLINE_LIFT = 0.16f
    private const val OUTLINE_VARIANT_LIFT = 0.1f
    private const val VIVID_PAGE_TINT = 0.8f
    private const val LIGHT_SURFACE_MIX = 0.6f
    private const val LIGHT_VARIANT_MIX = 0.06f
    private const val PALE_SELECTION_MIX = 0.12f
    private const val VIVID_SELECTION_TINT = 0.35f
    private const val INK_SHADE = 0.6f
    private const val LIGHT_OUTLINE_MIX = 0.18f
    private const val LIGHT_OUTLINE_VARIANT_MIX = 0.1f
}
