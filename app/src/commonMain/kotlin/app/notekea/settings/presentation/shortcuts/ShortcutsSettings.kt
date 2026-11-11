package app.notekea.settings.presentation.shortcuts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaKeycaps
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsGroup
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsPage
import app.notekea.core.presentation.designsystem.components.NotekeaSettingsRow
import app.notekea.core.presentation.designsystem.components.NotekeaSpotlight
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.keys
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.resources.Res
import app.notekea.resources.settings_section_shortcuts
import app.notekea.resources.settings_shortcuts_hint
import app.notekea.settings.presentation.about.AboutShortcut
import app.notekea.settings.presentation.about.AboutShortcuts
import org.jetbrains.compose.resources.stringResource

/** Settings → Shortcuts: every keyboard shortcut of this system, read-only, in About's groups. */
@Composable
internal fun ShortcutsSettings(
    platformConventions: PlatformConventions,
    spotlight: SettingsSpotlight?,
    modifier: Modifier = Modifier
) {
    val operatingSystem = platformConventions.operatingSystem

    NotekeaSettingsPage(
        title = stringResource(resource = Res.string.settings_section_shortcuts),
        modifier = modifier
    ) {
        NotekeaSpotlight(
            shownAt = spotlight?.serialFor(place = SettingsPlace.SHORTCUTS_LIST)
        ) {
            val hint = stringResource(resource = Res.string.settings_shortcuts_hint)

            Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)) {
                val groups = AboutShortcuts.groups(conventions = platformConventions)

                groups.forEachIndexed { index, group ->
                    val footnote = hint.takeIf {
                        index == 0
                    }

                    NotekeaSettingsGroup(
                        rows = group.shortcuts.map { shortcut ->
                            {
                                ShortcutRow(
                                    shortcut = shortcut,
                                    keys = shortcut.shortcut.keys(operatingSystem)
                                )
                            }
                        },
                        header = stringResource(resource = group.title),
                        footnote = footnote
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    shortcut: AboutShortcut,
    keys: List<String>
) {
    NotekeaSettingsRow(label = stringResource(resource = shortcut.label)) {
        NotekeaKeycaps(keys = keys)
    }
}

@Preview
@Composable
private fun ShortcutsSettingsPreview() {
    NotekeaPreviewThemes {
        ShortcutsSettings(
            platformConventions = PreviewPlatformConventions.linux,
            spotlight = null
        )
    }
}
