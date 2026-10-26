package app.notekea.notes.data

import com.ghost.serialization.annotations.GhostSerialization

/** The part of Obsidian's `.obsidian/app.json` that Notekea reads; every other setting is ignored. */
@GhostSerialization
data class ObsidianAppSettingsDto(
    val attachmentFolderPath: String? = null
)
