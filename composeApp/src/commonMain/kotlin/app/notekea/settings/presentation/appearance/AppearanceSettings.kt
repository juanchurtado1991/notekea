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
import app.notekea.core.presentation.designsystem.components.NotekeaThemeCard
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.ColorThemeSchemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.appearance_mode
import app.notekea.resources.appearance_mode_dark
import app.notekea.resources.appearance_mode_hint
import app.notekea.resources.appearance_mode_light
import app.notekea.resources.appearance_mode_system
import app.notekea.resources.appearance_reduce_motion
import app.notekea.resources.appearance_reduce_motion_hint
import app.notekea.resources.appearance_save_failed
import app.notekea.resources.appearance_theme
import app.notekea.resources.settings_section_appearance
import app.notekea.resources.theme_clay
import app.notekea.resources.theme_graphite
import app.notekea.resources.theme_honey
import app.notekea.resources.theme_iris
import app.notekea.resources.theme_lagoon
import app.notekea.resources.theme_rose
import app.notekea.resources.theme_sage
import app.notekea.resources.theme_workspace_blue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val THEMES_PER_ROW = 3

/**
 * Settings → Appearance (`ROADMAP.md` 2.12, design A): the mode (System, Light or Dark), reduce motion and the color themes as a grid
 * of cards, three per row, each with its light and dark sketch; the chosen one is outlined and checked. It scrolls when
 * the dialog is too short for it.
 */
@Composable
fun AppearanceSettings(
    state: AppearanceState,
    onIntent: (AppearanceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    NotekeaScrollArea(state = scrollState, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
                .padding(all = NotekeaDimensions.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            Text(
                text = stringResource(resource = Res.string.settings_section_appearance),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (state.hasSaveFailed) {
                Text(
                    text = stringResource(resource = Res.string.appearance_save_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            ModeChoice(mode = state.appearance.mode, onIntent = onIntent)
            ReduceMotionChoice(isOn = state.appearance.reduceMotion, onIntent = onIntent)
            ThemeChoice(theme = state.appearance.theme, onIntent = onIntent)
        }
    }
}

@Composable
private fun ModeChoice(
    mode: ThemeMode,
    onIntent: (AppearanceIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        GroupLabel(text = stringResource(resource = Res.string.appearance_mode))
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
        Text(
            text = stringResource(resource = Res.string.appearance_mode_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReduceMotionChoice(
    isOn: Boolean,
    onIntent: (AppearanceIntent) -> Unit
) {
    val label = stringResource(resource = Res.string.appearance_reduce_motion)
    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaCheckbox(
            checked = isOn,
            onClick = { onIntent(AppearanceIntent.OnReduceMotionToggle) },
            label = label
        )
        Column {
            GroupLabel(text = label)
            Text(
                text = stringResource(resource = Res.string.appearance_reduce_motion_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeChoice(
    theme: ColorTheme,
    onIntent: (AppearanceIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
        GroupLabel(text = stringResource(resource = Res.string.appearance_theme))
        ColorTheme.entries.chunked(size = THEMES_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
                row.forEach { each ->
                    NotekeaThemeCard(
                        name = stringResource(resource = nameOf(theme = each)),
                        light = ColorThemeSchemes.of(theme = each, darkTheme = false),
                        dark = ColorThemeSchemes.of(theme = each, darkTheme = true),
                        isSelected = each == theme,
                        onClick = { onIntent(AppearanceIntent.OnThemeChoose(theme = each)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupLabel(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** The name a theme is shown with. */
private fun nameOf(
    theme: ColorTheme
): StringResource = when (theme) {
    ColorTheme.WORKSPACE_BLUE -> Res.string.theme_workspace_blue
    ColorTheme.GRAPHITE -> Res.string.theme_graphite
    ColorTheme.SAGE -> Res.string.theme_sage
    ColorTheme.LAGOON -> Res.string.theme_lagoon
    ColorTheme.IRIS -> Res.string.theme_iris
    ColorTheme.ROSE -> Res.string.theme_rose
    ColorTheme.CLAY -> Res.string.theme_clay
    ColorTheme.HONEY -> Res.string.theme_honey
}

private val MODES = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)

@Preview
@Composable
private fun AppearanceSettingsPreview() {
    NotekeaPreviewThemes {
        AppearanceSettings(
            state = AppearanceState(appearance = Appearance(theme = ColorTheme.LAGOON)),
            onIntent = {}
        )
    }
}
