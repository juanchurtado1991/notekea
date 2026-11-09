package app.notekea.settings.presentation

import app.notekea.resources.Res
import app.notekea.resources.settings_section_ai
import app.notekea.resources.settings_section_appearance
import org.jetbrains.compose.resources.StringResource

/** The sections of Settings that can be chosen, in the order of the list, by their [label]. */
enum class SettingsSection(val label: StringResource) {
    APPEARANCE(label = Res.string.settings_section_appearance),
    AI_MODEL(label = Res.string.settings_section_ai)
}
