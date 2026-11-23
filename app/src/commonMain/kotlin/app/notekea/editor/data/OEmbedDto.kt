package app.notekea.editor.data

import com.ghost.serialization.annotations.GhostName
import com.ghost.serialization.annotations.GhostSerialization

/** YouTube's oEmbed answer: what a pasted link's card shows; a field it leaves out is null. */
@GhostSerialization
data class OEmbedDto(
    val title: String? = null,
    @GhostName("thumbnail_url") val thumbnailUrl: String? = null
)
