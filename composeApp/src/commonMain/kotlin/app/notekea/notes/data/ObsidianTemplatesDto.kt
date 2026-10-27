package app.notekea.notes.data

import com.ghost.serialization.annotations.GhostSerialization

/** Obsidian's `.obsidian/templates.json`, which the Templates plugin writes; a field it leaves out is null. */
@GhostSerialization
data class ObsidianTemplatesDto(
    val folder: String? = null,
    val dateFormat: String? = null,
    val timeFormat: String? = null
)
