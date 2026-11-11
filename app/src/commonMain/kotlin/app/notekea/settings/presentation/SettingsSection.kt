package app.notekea.settings.presentation

import app.notekea.core.settings.domain.SettingsTopic
import app.notekea.resources.Res
import app.notekea.resources.settings_section_ai
import app.notekea.resources.settings_section_appearance
import app.notekea.resources.settings_section_sync
import org.jetbrains.compose.resources.StringResource

/** The sections of Settings that can be chosen, in the order of the list, by their [label]; asked about as [topic]. */
enum class SettingsSection(
    val label: StringResource,
    val topic: SettingsTopic
) {
    APPEARANCE(
        label = Res.string.settings_section_appearance,
        topic = SettingsTopic.APPEARANCE
    ),
    AI_MODEL(
        label = Res.string.settings_section_ai,
        topic = SettingsTopic.AI_MODEL
    ),
    SYNC(
        label = Res.string.settings_section_sync,
        topic = SettingsTopic.SYNC
    );

    companion object {
        /** The section a question about [topic] opens. */
        fun of(topic: SettingsTopic): SettingsSection = entries.first {
            it.topic == topic
        }
    }
}
