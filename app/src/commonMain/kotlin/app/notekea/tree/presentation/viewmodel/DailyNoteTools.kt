package app.notekea.tree.presentation.viewmodel

import app.notekea.tree.domain.GetDailyNoteSettingsUseCase
import app.notekea.tree.domain.OpenDailyNoteUseCase

/** What the sidebar needs for daily notes, given together so its constructor stays short. */
class DailyNoteTools(
    val getDailyNoteSettings: GetDailyNoteSettingsUseCase,
    val openDailyNote: OpenDailyNoteUseCase
)
