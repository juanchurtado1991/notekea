package app.notekea.settings.presentation

import androidx.compose.ui.graphics.vector.ImageVector
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.settings.domain.SettingsTopic
import app.notekea.resources.Res
import app.notekea.resources.settings_section_about
import app.notekea.resources.settings_section_ai
import app.notekea.resources.settings_section_appearance
import app.notekea.resources.settings_section_editor
import app.notekea.resources.settings_section_files
import app.notekea.resources.settings_section_general
import app.notekea.resources.settings_section_shortcuts
import app.notekea.resources.settings_section_sync
import org.jetbrains.compose.resources.StringResource

/** The sections of Settings, in the order of its sidebar, by their [label] and [icon]; asked about as [topic]. */
enum class SettingsSection(
    val label: StringResource,
    val icon: ImageVector,
    val topic: SettingsTopic
) {
    GENERAL(
        label = Res.string.settings_section_general,
        icon = NotekeaIcons.Settings,
        topic = SettingsTopic.GENERAL
    ),
    APPEARANCE(
        label = Res.string.settings_section_appearance,
        icon = NotekeaIcons.SettingsAppearance,
        topic = SettingsTopic.APPEARANCE
    ),
    EDITOR(
        label = Res.string.settings_section_editor,
        icon = NotekeaIcons.SettingsEditor,
        topic = SettingsTopic.EDITOR
    ),
    FILES(
        label = Res.string.settings_section_files,
        icon = NotekeaIcons.Files,
        topic = SettingsTopic.FILES
    ),
    AI_MODEL(
        label = Res.string.settings_section_ai,
        icon = NotekeaIcons.ChatAnswer,
        topic = SettingsTopic.AI_MODEL
    ),
    SYNC(
        label = Res.string.settings_section_sync,
        icon = NotekeaIcons.Syncing,
        topic = SettingsTopic.SYNC
    ),
    SHORTCUTS(
        label = Res.string.settings_section_shortcuts,
        icon = NotekeaIcons.SettingsShortcuts,
        topic = SettingsTopic.SHORTCUTS
    ),
    ABOUT(
        label = Res.string.settings_section_about,
        icon = NotekeaIcons.About,
        topic = SettingsTopic.ABOUT
    );

    companion object {
        /** The section a question about [topic] opens. */
        fun of(topic: SettingsTopic): SettingsSection = entries.first {
            it.topic == topic
        }
    }
}
