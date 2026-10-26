package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.GetDailyNoteSettingsUseCase
import app.notekea.notes.domain.OpenDailyNoteUseCase

/** What the sidebar needs for daily notes (`ROADMAP.md` 1.10), given together so its constructor stays short. */
class DailyNoteTools(
    val getDailyNoteSettings: GetDailyNoteSettingsUseCase,
    val openDailyNote: OpenDailyNoteUseCase
)
