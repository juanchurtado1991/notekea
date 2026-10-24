package app.notekea.notes.data

import com.ghost.serialization.annotations.GhostSerialization
import com.ghost.serialization.annotations.GhostYamlSerialization

/** One frontmatter key whose value is a list (`tags: [a, b]` or a `- a` block), renamed to `value` before decoding. */
@GhostSerialization
@GhostYamlSerialization
data class FrontmatterListDto(
    val value: List<String>? = null
)
