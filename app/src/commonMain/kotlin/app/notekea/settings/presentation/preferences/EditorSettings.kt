package app.notekea.settings.presentation.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.LineWidth
import app.notekea.core.presentation.designsystem.components.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.NotekeaPopUpButton
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.components.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.settings_editor_hint
import app.notekea.resources.settings_line_width
import app.notekea.resources.settings_line_width_full
import app.notekea.resources.settings_line_width_readable
import app.notekea.resources.settings_line_width_wide
import app.notekea.resources.settings_section_editor
import app.notekea.resources.settings_text_size
import app.notekea.resources.settings_text_size_points
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Settings → Editor: the note's text size and how wide its lines go (5.2). */
@Composable
internal fun EditorSettings(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?,
    modifier: Modifier = Modifier
) {
    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_editor),
        modifier = modifier
    ) {
        NotekeaSettingsGroup(
            rows = listOf(
                {
                    TextSize(
                        preferences = preferences,
                        onIntent = onIntent,
                        spotlight = spotlight
                    )
                },
                {
                    Width(
                        preferences = preferences,
                        onIntent = onIntent,
                        spotlight = spotlight
                    )
                }
            ),
            footnote = stringResource(resource = Res.string.settings_editor_hint)
        )
    }
}

@Composable
private fun TextSize(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val label = stringResource(resource = Res.string.settings_text_size)

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.EDITOR_TEXT_SIZE)
    ) {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = stringResource(
                    resource = Res.string.settings_text_size_points,
                    preferences.textSize
                ),
                choices = AppPreferences.TEXT_SIZES.map {
                    NotekeaMenuItem(
                        label = stringResource(resource = Res.string.settings_text_size_points, it),
                        onClick = { onIntent(PreferencesIntent.OnTextSizeChoose(size = it)) },
                        isChecked = it == preferences.textSize
                    )
                },
                label = label
            )
        }
    }
}

@Composable
private fun Width(
    preferences: AppPreferences,
    onIntent: (PreferencesIntent) -> Unit,
    spotlight: SettingsSpotlight?
) {
    val label = stringResource(resource = Res.string.settings_line_width)

    NotekeaSpotlight(
        shownAt = spotlight?.serialFor(place = SettingsPlace.EDITOR_LINE_WIDTH)
    ) {
        NotekeaSettingsRow(label = label) {
            NotekeaPopUpButton(
                choice = stringResource(resource = nameOf(width = preferences.lineWidth)),
                choices = LineWidth.entries.map {
                    NotekeaMenuItem(
                        label = stringResource(resource = nameOf(width = it)),
                        onClick = { onIntent(PreferencesIntent.OnLineWidthChoose(width = it)) },
                        isChecked = it == preferences.lineWidth
                    )
                },
                label = label
            )
        }
    }
}

private fun nameOf(width: LineWidth): StringResource = when (width) {
    LineWidth.READABLE -> Res.string.settings_line_width_readable
    LineWidth.WIDE -> Res.string.settings_line_width_wide
    LineWidth.FULL -> Res.string.settings_line_width_full
}

@Preview
@Composable
private fun EditorSettingsPreview() {
    NotekeaPreviewThemes {
        EditorSettings(
            preferences = AppPreferences(),
            onIntent = {},
            spotlight = null
        )
    }
}
