package app.notekea.notes.data

import com.ghost.serialization.annotations.GhostSerialization

/** Obsidian's `.obsidian/daily-notes.json`, which the Daily notes plugin writes; a field it leaves out is null. */
@GhostSerialization
data class ObsidianDailyNotesDto(
    val folder: String? = null,
    val format: String? = null,
    val template: String? = null
)
