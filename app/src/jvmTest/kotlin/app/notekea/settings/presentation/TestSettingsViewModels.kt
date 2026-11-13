package app.notekea.settings.presentation

import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.logging.domain.FakeLogStore
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.FakeVaultSettingsWriter
import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.settings.domain.GetDiagnosticsUseCase
import app.notekea.settings.presentation.about.AboutViewModel
import app.notekea.settings.presentation.appearance.AppearanceViewModel
import app.notekea.settings.presentation.files.FilesSettingsViewModel
import app.notekea.settings.presentation.help.SettingsHelpViewModel
import app.notekea.settings.presentation.preferences.PreferencesViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlin.time.Clock

/** Settings' view models over fakes, for the screen tests that open the dialog without Koin. */
fun testSettingsViewModels(
    appearanceStore: AppearanceStore = FakeAppearanceStore()
): SettingsViewModels = SettingsViewModels(
    appearance = AppearanceViewModel(appearanceStore = appearanceStore),
    preferences = PreferencesViewModel(store = FakeAppPreferencesStore()),
    files = FilesSettingsViewModel(
        reader = FakeVaultSettingsReader(),
        writer = FakeVaultSettingsWriter(),
        attachmentStore = FakeAttachmentStore(),
        localCalendar = FakeLocalCalendar(),
        clock = Clock.System,
        vaultPath = null
    ),
    about = AboutViewModel(
        getDiagnostics = GetDiagnosticsUseCase(
            systemEnvironment = FakeSystemEnvironment(
                operatingSystemName = "Linux",
                userHome = "/home/ana"
            ),
            logStore = FakeLogStore()
        ),
        clipboard = FakeTextClipboard()
    ),
    help = SettingsHelpViewModel(helper = { _, _, _ -> emptyFlow() })
)
