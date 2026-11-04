package app.notekea.tree.presentation

import app.notekea.tree.domain.GetDailyNoteSettingsUseCase
import app.notekea.tree.domain.OpenDailyNoteUseCase

/** What the sidebar needs for daily notes (`ROADMAP.md` 1.10), given together so its constructor stays short. */
class DailyNoteTools(
    val getDailyNoteSettings: GetDailyNoteSettingsUseCase,
    val openDailyNote: OpenDailyNoteUseCase
)
