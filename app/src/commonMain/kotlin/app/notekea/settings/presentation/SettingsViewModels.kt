package app.notekea.settings.presentation

import androidx.compose.runtime.Composable
import app.notekea.settings.presentation.about.AboutViewModel
import app.notekea.settings.presentation.appearance.AppearanceViewModel
import app.notekea.settings.presentation.files.FilesSettingsViewModel
import app.notekea.settings.presentation.help.SettingsHelpViewModel
import app.notekea.settings.presentation.preferences.PreferencesViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The view models of Settings' own sections and of Ask about Settings, given together to the dialog. */
class SettingsViewModels(
    val appearance: AppearanceViewModel,
    val preferences: PreferencesViewModel,
    val files: FilesSettingsViewModel,
    val about: AboutViewModel,
    val help: SettingsHelpViewModel
) {
    companion object {
        /** Settings' view models from Koin, Files' for the vault at [vaultPath]. */
        @Composable
        fun fromKoin(vaultPath: String?): SettingsViewModels = SettingsViewModels(
            appearance = koinViewModel(),
            preferences = koinViewModel(),
            files = koinViewModel(key = "FilesSettings:$vaultPath") { parametersOf(vaultPath) },
            about = koinViewModel(),
            help = koinViewModel()
        )
    }
}
