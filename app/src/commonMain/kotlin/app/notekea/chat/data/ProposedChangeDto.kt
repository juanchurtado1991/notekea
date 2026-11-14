package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** A change of a proposal as the history keeps it; its kind by name. */
@GhostSerialization
data class ProposedChangeDto(
    val kind: String,
    val title: String? = null,
    val start: String = "",
    val notes: List<String> = emptyList()
)
