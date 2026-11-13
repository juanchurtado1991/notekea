package app.notekea.settings.presentation.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.presentation.designsystem.components.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.NotekeaScrollArea
import app.notekea.core.presentation.designsystem.components.NotekeaSegment
import app.notekea.core.presentation.designsystem.components.NotekeaSegmentedControl
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.components.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.components.NotekeaSwitch
import app.notekea.core.presentation.designsystem.components.NotekeaThemeCard
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.ColorThemeSchemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.appearance_mode
import app.notekea.resources.appearance_mode_dark
import app.notekea.resources.appearance_mode_hint
import app.notekea.resources.appearance_mode_light
import app.notekea.resources.appearance_mode_system
import app.notekea.resources.appearance_reduce_motion
import app.notekea.resources.appearance_reduce_motion_hint
import app.notekea.resources.appearance_save_failed
import app.notekea.resources.appearance_system_accent
import app.notekea.resources.appearance_system_accent_hint
import app.notekea.resources.appearance_theme
import app.notekea.resources.settings_section_appearance
import app.notekea.resources.theme_desert
import app.notekea.resources.theme_forest
import app.notekea.resources.theme_lavender
import app.notekea.resources.theme_ocean
import app.notekea.resources.theme_polar
import app.notekea.resources.theme_storm
import app.notekea.resources.theme_workspace_blue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val THEMES_PER_ROW = 3

/**
 * Settings → Appearance: the mode (System, Light or Dark), reduce motion and the color themes as a grid
 * of cards, three per row, each with its light and dark sketch; the chosen one is outlined and checked. It scrolls when
 * the dialog is too short for it.
 */
@Composable
fun AppearanceSettings(
    state: AppearanceState,
    onIntent: (AppearanceIntent) -> Unit,
    modifier: Modifier = Modifier,
    spotlight: SettingsSpotlight? = null
) {
    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_appearance),
        modifier = modifier
    ) {
        if (state.hasSaveFailed) {
            Text(
                text = stringResource(resource = Res.string.appearance_save_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        NotekeaSettingsGroup(
            rows = listOf(
                {
                    NotekeaSpotlight(
                        shownAt = spotlight?.serialFor(place = SettingsPlace.APPEARANCE_MODE)
                    ) {
                        ModeChoice(
                            mode = state.appearance.mode,
                            onIntent = onIntent
                        )
                    }
                },
                {
                    val shown = spotlight?.serialFor(place = SettingsPlace.APPEARANCE_REDUCE_MOTION)

                    NotekeaSpotlight(shownAt = shown) {
                        ReduceMotionChoice(
                            isOn = state.appearance.reduceMotion,
                            onIntent = onIntent
                        )
                    }
                }
            ),
            footnote = stringResource(resource = Res.string.appearance_mode_hint)
        )

        NotekeaSpotlight(
            shownAt = spotlight?.serialFor(place = SettingsPlace.APPEARANCE_THEME)
        ) {
            NotekeaSettingsGroup(
                rows = listOf(
                    {
                        ThemeChoice(
                            theme = state.appearance.theme,
                            onIntent = onIntent
                        )
                    },
                    {
                        val shown =
                            spotlight?.serialFor(place = SettingsPlace.APPEARANCE_SYSTEM_ACCENT)

                        NotekeaSpotlight(shownAt = shown) {
                            SystemAccentChoice(
                                isOn = state.appearance.usesSystemAccent,
                                onIntent = onIntent
                            )
                        }
                    }
                ),
                header = stringResource(resource = Res.string.appearance_theme),
                footnote = stringResource(resource = Res.string.appearance_system_accent_hint)
            )
        }
    }
}

@Composable
private fun ModeChoice(
    mode: ThemeMode,
    onIntent: (AppearanceIntent) -> Unit
) {
    NotekeaSettingsRow(label = stringResource(resource = Res.string.appearance_mode)) {
        NotekeaSegmentedControl(
            segments = listOf(
                NotekeaSegment(
                    icon = NotekeaIcons.ModeSystem,
                    label = stringResource(resource = Res.string.appearance_mode_system)
                ),
                NotekeaSegment(
                    icon = NotekeaIcons.ModeLight,
                    label = stringResource(resource = Res.string.appearance_mode_light)
                ),
                NotekeaSegment(
                    icon = NotekeaIcons.ModeDark,
                    label = stringResource(resource = Res.string.appearance_mode_dark)
                )
            ),
            selectedIndex = MODES.indexOf(element = mode),
            onSelect = { onIntent(AppearanceIntent.OnModeChoose(mode = MODES[it])) }
        )
    }
}

@Composable
private fun ReduceMotionChoice(
    isOn: Boolean,
    onIntent: (AppearanceIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.appearance_reduce_motion)

    NotekeaSettingsRow(
        label = label,
        detail = stringResource(resource = Res.string.appearance_reduce_motion_hint)
    ) {
        NotekeaSwitch(
            isOn = isOn,
            onToggle = { onIntent(AppearanceIntent.OnReduceMotionToggle) },
            label = label
        )
    }
}

@Composable
private fun SystemAccentChoice(
    isOn: Boolean,
    onIntent: (AppearanceIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.appearance_system_accent)

    NotekeaSettingsRow(label = label) {
        NotekeaSwitch(
            isOn = isOn,
            onToggle = { onIntent(AppearanceIntent.OnSystemAccentToggle) },
            label = label
        )
    }
}

@Composable
private fun ThemeChoice(
    theme: ColorTheme,
    onIntent: (AppearanceIntent) -> Unit
) {
    Column(
        modifier = Modifier.padding(all = NotekeaDimensions.SpacingM),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        ColorTheme
            .entries
            .chunked(size = THEMES_PER_ROW)
            .forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
                ) {
                    row.forEach { each ->
                        NotekeaThemeCard(
                            name = stringResource(resource = nameOf(theme = each)),
                            light = ColorThemeSchemes.of(
                                theme = each,
                                darkTheme = false
                            ),
                            dark = ColorThemeSchemes.of(
                                theme = each,
                                darkTheme = true
                            ),
                            isSelected = each == theme,
                            onClick = { onIntent(AppearanceIntent.OnThemeChoose(theme = each)) }
                        )
                    }
                }
            }
    }
}

/** The name a theme is shown with. */
private fun nameOf(theme: ColorTheme): StringResource = when (theme) {
    ColorTheme.WORKSPACE_BLUE -> Res.string.theme_workspace_blue
    ColorTheme.POLAR -> Res.string.theme_polar
    ColorTheme.DESERT -> Res.string.theme_desert
    ColorTheme.STORM -> Res.string.theme_storm
    ColorTheme.FOREST -> Res.string.theme_forest
    ColorTheme.OCEAN -> Res.string.theme_ocean
    ColorTheme.LAVENDER -> Res.string.theme_lavender
}

private val MODES = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)

@Preview
@Composable
private fun AppearanceSettingsPreview() {
    NotekeaPreviewThemes {
        AppearanceSettings(
            state = AppearanceState(appearance = Appearance(theme = ColorTheme.OCEAN)),
            onIntent = {}
        )
    }
}
