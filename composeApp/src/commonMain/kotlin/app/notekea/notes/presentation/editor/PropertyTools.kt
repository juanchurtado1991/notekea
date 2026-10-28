package app.notekea.notes.presentation.editor

import app.notekea.core.domain.time.LocalCalendar
import kotlin.time.Clock

/**
 * What the editor needs for the properties above the note (`ROADMAP.md` 1.14): today's date where the user is, for a
 * new date property; given together so its constructor stays short.
 */
class PropertyTools(
    val localCalendar: LocalCalendar,
    val clock: Clock
)
