package app.notekea.notes.data

import com.ghost.serialization.annotations.GhostSerialization
import com.ghost.serialization.annotations.GhostYamlSerialization

/** One frontmatter key whose value is a single scalar (`title: Pricing ideas`), renamed to `value` before decoding. */
@GhostSerialization
@GhostYamlSerialization
data class FrontmatterTextDto(
    val value: String? = null
)
